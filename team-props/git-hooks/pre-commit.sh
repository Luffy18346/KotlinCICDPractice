#!/bin/bash

# Adapted from https://proandroiddev.com/ooga-chaka-git-hooks-to-enforce-code-quality-11ce8d0d23cb

echo "*********************************************************"
echo "Running git pre-commit hook. Running Static analysis... "
echo "*********************************************************"

lint="./gradlew ktlintFormat detekt ktlintCheck --daemon"
$lint

status=$?

if [ "$status" = 0 ] ; then
    echo "Static analysis found no problems."
    test_command="./gradlew test connectedAndroidTest"
    $test_command
    test_result=$?
    if [ $test_result -ne 0 ]; then
        echo "Tests failed: $test_command"
        exit 1
    fi
    echo "Tests passed."
    exit 0
else
    echo 1>&2 "Static analysis found violations it could not fix."
    echo 1>&2 "There are code formatting or code quality issues."
    echo 1>&2 "You can use the option --no-verify in commits to bypass this hook (although this is not recommended)"
    exit 1
fi
