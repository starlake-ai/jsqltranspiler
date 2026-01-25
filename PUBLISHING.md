# Publishing to Maven Central

This project is configured to publish to Maven Central using the `com.vanniktech.maven.publish` Gradle plugin.

## Prerequisites

1.  **Sonatype OSSRH Account**: You need an account on [s01.oss.sonatype.org](https://s01.oss.sonatype.org/) (or `oss.sonatype.org` depending on when you registered).
2.  **GPG Key**: You need a GPG key pair to sign the artifacts.

## Configuration

You need to configure your credentials and signing key. It is recommended to put these in your global `~/.gradle/gradle.properties` file so they are not committed to the repository.

Add the following properties to `~/.gradle/gradle.properties`:

```properties
# Sonatype Credentials
mavenCentralUsername=your_sonatype_username
mavenCentralPassword=your_sonatype_password

# Signing Configuration
signing.keyId=LAST_8_CHARS_OF_YOUR_GPG_KEY_ID
signing.password=your_gpg_passphrase
signing.secretKeyRingFile=/Users/youruser/.gnupg/secring.gpg
```

_Note: If you are using GPG 2.x, you might need to export your secret key to a compatible format if `secring.gpg` does not exist._

Alternatively, you can provide the key content via environment variables or properties:
`signing.key` (ARMORED key content) and `signing.password`.

## Publishing a Release

To publish a release version (non-SNAPSHOT):

1.  Ensure you are on the branch you want to release.
2.  Run the `publish.sh` script:

```bash
./publish.sh
```

This script sets the `RELEASE=1` environment variable, which:

- Generates a release version (strips `-SNAPSHOT` suffix).
- Enables GPG signing.
- Publishes to the staging repository on Maven Central.
- Automatically closes and releases the staging repository (if configured so, otherwise you might need to login to Nexus to release it).

## Publishing a Snapshot

To publish a SNAPSHOT version:

```bash
./gradlew publish
```

(Without `RELEASE=1`, it defaults to SNAPSHOT and may skip strong signing requirements or publish to the snapshot repository).
