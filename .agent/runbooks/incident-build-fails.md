# Incident: Build Fails

Steps:

1. Identify platform and command.
2. Check whether failure is dependency, core artifact, signing, generated file, or platform toolchain.
3. For generated Dart failures, inspect source and generated files together.
4. For core failures, inspect `setup.dart`, `core/go.mod`, `libclash/`, and Android CMake if relevant.
5. For signing failures, report variable names and file areas only.
6. Apply the smallest platform-specific fix.
7. Validate with the narrowest failing command first.
