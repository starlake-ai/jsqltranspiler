# Publishing to Maven Central

This project is configured to publish to Maven Central using the `com.vanniktech.maven.publish` Gradle plugin.

## Prerequisites

1.  **Central Portal Account**: You need an account on [central.sonatype.com](https://central.sonatype.com/) with a publishing token for the `ai.starlake.jsqltranspiler` namespace.
2.  **GPG Key**: You need a GPG key pair to sign the artifacts.

## Configuration

You need to configure your credentials and signing key. It is recommended to put these in your global `~/.gradle/gradle.properties` file so they are not committed to the repository.

Add the following properties to `~/.gradle/gradle.properties`:

```properties
# Central Portal token
mavenCentralUsername=your_token_username
mavenCentralPassword=your_token_password

# Signing Configuration
signing.keyId=LAST_8_CHARS_OF_YOUR_GPG_KEY_ID
signing.password=your_gpg_passphrase
signing.secretKeyRingFile=/Users/youruser/.gnupg/secring.gpg
```

_Note: If you are using GPG 2.x, you might need to export your secret key to a compatible format if `secring.gpg` does not exist._

Alternatively, you can provide the key content via environment variables or properties:
`signing.key` (ARMORED key content) and `signing.password`.

## Versioning

The version is derived from `git describe`: on a tag `vX.Y` the version is `X.Y`; any commit after a tag bumps the minor (`v1.10` + commits → `1.11`). Without `RELEASE=1` a `-SNAPSHOT` suffix is appended.

## jsqlparser dependency

The project consumes JSQLParser snapshots of the manticore branch (`com.manticore-projects.jsqlformatter:jsqlparser`):

The dependency is declared as a version range (e.g. `[5.4.162,)`).

- **Snapshot builds** also register `mavenLocal()` and the central snapshots repository, so the range resolves to the newest snapshot.
- **Release builds** (`RELEASE=1`) register neither, so the range resolves to the newest release on Maven Central. Release POMs must not pin a snapshot: Central rejects them ("Dependencies to SNAPSHOT versions not allowed"), the snapshots repository prunes artifacts, and consumers do not have it configured. As a safety net, the POM generation fails a release build if a snapshot still resolves.

In both cases the published POM pins the concrete resolved version instead of the range.

## Publishing a Snapshot

```bash
./publish.sh
```

Without `RELEASE` set, the script publishes the current `X.Y-SNAPSHOT` directly to the central snapshots repository (`https://central.sonatype.com/repository/maven-snapshots/`). No manual step is needed; the snapshot is live once the upload completes.

## Publishing a Release

```bash
RELEASE=1 ./publish.sh
```

This:

1. Refuses to run if the workspace has uncommitted tracked changes.
2. Computes the release version from `git describe` and refuses to run if the tag `vX.Y` already exists.
3. Creates the tag `vX.Y` locally.
4. Builds, signs, and uploads the bundle to the Central Portal.
5. Pushes the tag to `origin` (only if the upload succeeded).

The upload creates a **USER_MANAGED deployment**: go to [central.sonatype.com/publishing/deployments](https://central.sonatype.com/publishing/deployments) and press **Publish** to release it to Maven Central.

Note: the script pushes only the tag, not the branch. Push `main` separately.
