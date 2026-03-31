# Upgrade Plan: blood-donation-system (20260331184752)

- **Generated**: 2026-04-01 00:18:30 +05:30
- **HEAD Branch**: main
- **HEAD Commit ID**: 0b7983e

## Available Tools

**JDKs**
- JDK 17.0.18: C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot\bin (baseline step)
- JDK 25.0.1: C:\Program Files\Java\jdk-25\bin (target Java runtime for upgrade and final validation)

**Build Tools**
- Maven 3.9.12: C:\Program Files\apache-maven-3.9.12\bin
- Maven Wrapper: Not present

## Guidelines

- Upgrade Java runtime to the latest LTS version.

## Options

- Working branch: appmod/java-upgrade-20260331184752
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java from 17 to 25 (latest LTS requested by user).
- Keep application behavior unchanged while maintaining successful compile and test execution.

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 17 | 25 | User requested latest LTS runtime |
| Spring Boot Parent | 3.2.0 | 3.5.0 | Align with newer JDK baseline and maintained release line |
| Maven | 3.9.12 | 3.9+ | Compatible for this upgrade path |
| maven-compiler-plugin | 3.11.0 | 3.14.0 | Newer plugin has improved Java 25 toolchain support |
| maven-surefire-plugin | 3.1.2 | 3.2.5 | Ensure stable test execution on latest JDKs |
| Lombok | 1.18.30 | 1.18.38 | Better compatibility with latest JDK internals |

### Derived Upgrades

- Update Maven compiler release from 17 to 25 to produce Java 25 bytecode.
- Upgrade Spring Boot parent from 3.2.0 to 3.5.0 to stay on a current compatible line with modern JDKs.
- Upgrade maven-compiler-plugin to 3.14.0 for latest Java language level handling.
- Upgrade maven-surefire-plugin to 3.2.5 and run tests (remove forced skip) so final validation can prove test pass rate.
- Upgrade Lombok to 1.18.38 for improved support with Java 25.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Ensure required JDK and Maven are available before executing upgrade.
  - **Changes to Make**:
    - [ ] Verify JDK 17 and JDK 25 availability.
    - [ ] Verify Maven 3.9.12 availability.
    - [ ] Record selected JDK/build tool paths in progress tracking.
  - **Verification**:
    - Command: appmod-list-jdks and appmod-list-mavens
    - Expected: JDK 17, JDK 25, Maven 3.9.12 detected

- **Step 2: Setup Baseline**
  - **Rationale**: Capture pre-upgrade compile and test status to compare final outcome.
  - **Changes to Make**:
    - [ ] Build with current Java 17 configuration.
    - [ ] Execute baseline tests and capture pass rate.
  - **Verification**:
    - Command: mvn clean test -q
    - JDK: C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
    - Expected: Build/test baseline recorded

- **Step 3: Upgrade Runtime and Build Configuration to Java 25**
  - **Rationale**: Apply required version and plugin updates for target Java LTS runtime.
  - **Changes to Make**:
    - [ ] Set java.version and compiler release to 25.
    - [ ] Upgrade Spring Boot parent to 3.5.0.
    - [ ] Upgrade maven-compiler-plugin, maven-surefire-plugin, and Lombok.
    - [ ] Remove forced test skipping from surefire configuration.
  - **Verification**:
    - Command: mvn clean test-compile -q
    - JDK: C:\Program Files\Java\jdk-25
    - Expected: Compilation succeeds with Java 25

- **Step 4: Final Validation**
  - **Rationale**: Confirm all goals are met with successful compile and full test execution.
  - **Changes to Make**:
    - [ ] Verify all target versions in pom.xml.
    - [ ] Run clean build and full test suite on Java 25.
    - [ ] Fix any remaining compile/test issues until green.
  - **Verification**:
    - Command: mvn clean test -q
    - JDK: C:\Program Files\Java\jdk-25
    - Expected: Compilation success and 100% tests pass

## Key Challenges

- **Spring Boot Compatibility Drift**
  - **Challenge**: Current Spring Boot 3.2.0 is older and may have weaker compatibility with Java 25.
  - **Strategy**: Upgrade to Spring Boot 3.5.0 in the same step as Java 25 update and verify compile before final test pass.

- **Test Execution Previously Disabled**
  - **Challenge**: Existing surefire config skips tests, which hides regressions.
  - **Strategy**: Remove skipTests configuration and enforce final full test run on Java 25.
