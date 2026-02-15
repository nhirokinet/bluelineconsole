#!/usr/bin/env python3


import os
import pathlib
import platform
import subprocess
import sys
import xml.etree.ElementTree as ElementTree


# Check that dependencies and licenses does not go beyond what I realize.
def main():
    is_release = False
    executable = '.\\gradlew.bat' if platform.system() == 'Windows' else './gradlew'
    cmd = [executable, 'app:dependencies']
    output = subprocess.check_output(cmd).decode('utf-8')
    print("Command: " + " ".join(cmd))
    print("============================")
    print(output)
    print("============================")
    print()

    gradle_files = []
    for root, dirs, files in os.walk(os.path.join(pathlib.Path.home(), '.gradle')):
        gradle_files.append((root, dirs, files))

    warns = []

    libraries = []
    for line in output.split(os.linesep):
        if line == '':
            is_release = False

        if not is_release and not line.startswith("releaseRuntimeClasspath"):
            continue

        if line.startswith("releaseRuntimeClasspath"):
            is_release = True
            continue

        if line.startswith('+--- ') or line.startswith('\\--- ') or line.startswith('     ') or line.startswith('|    '):
            if line.startswith('     '):
                line = line[len('     '):]
            line = line.replace('|    ', '')
            line = line.replace('    ', '')
            line = line.strip()
            assert(line.startswith('+--- ') or line.startswith('\\--- ') or line.startswith('|    '))
            package_info = line[len('+--- '):].split(':')
            package_fullname = package_info[0]
            package_name = package_info[1]
            package_version = package_info[2].split(' -> ')[-1].split(' ')[0]

            library_dict = {
                'fullname': package_fullname,
                'name': package_name,
                'version': package_version,
            }

            if not package_fullname.startswith('androidx.')\
                    and package_name != 'kotlinx-coroutines-android'\
                    and not package_name.startswith('kotlin-stdlib')\
                    and not package_name.startswith('kotlin-coroutines')\
                    and not package_name.startswith('kotlinx-coroutines')\
                    and not (package_name == 'annotations' and package_fullname == 'org.jetbrains')\
                    and package_fullname != 'com.google.guava':
                print("Line: " + line)
                print('ERROR: Unknown package')
                sys.exit(1)

            pom_filename = None

            desired_filename = package_name + '-' + package_version + '.pom'
            for root, dirs, files in gradle_files:
                if desired_filename in files:
                    pom_filename = os.path.join(root, desired_filename)
                    break

            if pom_filename is None:
                print("Line: " + line)
                print(desired_filename + ' not found')
                sys.exit(1)

            pom = ElementTree.parse(pom_filename).getroot()
            ok = False
            for licenses_elem in pom.iter('{http://maven.apache.org/POM/4.0.0}licenses'):
                for license_elem in licenses_elem.findall('{http://maven.apache.org/POM/4.0.0}license'):
                    license_name = license_elem.find('{http://maven.apache.org/POM/4.0.0}name').text
                    if (license_name in [
                                         'The Apache Software License, Version 2.0',
                                         'The Apache License, Version 2.0',
                                        ]):
                        library_dict['license'] = license_name
                        ok = True

                    license_url = license_elem.find('{http://maven.apache.org/POM/4.0.0}url').text
                    if (license_url in [
                                        'https://www.apache.org/licenses/LICENSE-2.0.txt',
                                        'http://www.apache.org/licenses/LICENSE-2.0.txt',
                                       ]):
                        library_dict['license_url'] = license_url
                        ok = True

            if not ok:
                if (package_fullname in [
                                         'com.google.guava',
                                        ]):
                    library_dict['license'] = 'Unknown'
                    warns.append("Should be licensed under Apache License 2 but failed to find metadata: " + package_fullname + ": " + str(library_dict))
                else:
                    print('ERROR: Could not find proper license for ' + line)
                    sys.exit(1)

            libraries.append(library_dict)

        else:
            if is_release:
                print('Line: ' + line)
                raise Exception("Unknown format")

    print()
    if len(libraries) < 2:
        print('Less ok libraries than expected: %d' % ok_count)
        sys.exit(1)

    print('OK')
    print()
    prev_to_str = ''
    for l in sorted(libraries, key=lambda x: x['fullname']):
        now_to_str = str(l)
        if (prev_to_str == now_to_str):
            continue
        prev_to_str = now_to_str
        print(l["fullname"] + "version " + l['version'])
        print("  " + str(l))

    if len(warns) > 0:
        print()
        print("WARNINGS")
        for w in warns:
            print("  " + w)


if __name__ == '__main__':
    main()
