#!/usr/bin/env python3


import os
import subprocess
import sys
import xml.etree.ElementTree as ElementTree


# Check that dependencies and licenses does not go beyond what I realize.
def main():
    ok_count = 0

    is_release = False
    output = subprocess.check_output(['./gradlew', 'androidDependencies']).decode('utf-8')
    print(output)
    for line in output.split('\n'):
        if line == '':
            is_release = False

        if not is_release and line != 'release':
            continue

        if line == 'release':
            is_release = True

        if line.startswith('+--- ') or line.startswith('\--- '):
            package_info = line[len('+--- '):line.find('@')].split(':')
            print(package_info)
            package_fullname = package_info[0]
            package_name = package_info[1]
            package_version = package_info[2]

            if not package_fullname.startswith('androidx.')\
                    and package_name != 'kotlinx-coroutines-android'\
                    and package_name != 'kotlinx-coroutines-core-jvm'\
                    and not package_name.startswith('kotlin-stdlib')\
                    and not (package_name == 'annotations' and package_fullname == 'org.jetbrains')\
                    and package_fullname != 'com.google.guava':
                print('Unknown package')
                sys.exit(1)

            pom_filename = None

            desired_filename = package_name + '-' + package_version + '.pom'
            for root, dirs, files in os.walk(os.path.join(os.environ['HOME'], '.gradle')):
                if desired_filename in files:
                    pom_filename = os.path.join(root, desired_filename)
                    break

            if pom_filename is None:
                print(desired_filename + ' not found')
                sys.exit(1)

            pom = ElementTree.parse(pom_filename).getroot()
            ok = False
            for licenses_elem in pom.iter('{http://maven.apache.org/POM/4.0.0}licenses'):
                for license_elem in licenses_elem.findall('{http://maven.apache.org/POM/4.0.0}license'):
                    license_name = license_elem.find('{http://maven.apache.org/POM/4.0.0}name').text
                    print('  License: ' + license_name)
                    assert(license_name == 'The Apache Software License, Version 2.0' or
                           license_name == 'The Apache License, Version 2.0')
                    license_url = license_elem.find('{http://maven.apache.org/POM/4.0.0}url').text
                    print('  License URL: ' + license_url)
                    assert(license_url == 'https://www.apache.org/licenses/LICENSE-2.0.txt' or
                           license_url == 'http://www.apache.org/licenses/LICENSE-2.0.txt')
                    ok = True

            if not ok:
                print('Could not find proper license for ' + line)
                sys.exit(1)

            ok_count += 1

    if ok_count < 2:
        print('Less ok libraries than expected: %d' % ok_count)
        sys.exit(1)


if __name__ == '__main__':
    main()
