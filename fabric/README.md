# Future Fabric support

This module is disabled in `settings.gradle`. It is a template, not a working Fabric port.
Create Fabric has no published Minecraft 1.21.1 release at the time of scaffolding.

When a compatible release exists:

1. Pin its Create/Fabric dependencies and the Loom plugin, then enable this module.
2. Implement the client entrypoint, clipboard data access, rendering registration,
   input interception, and Create packet sending against that release.
3. Reuse the layout, surface transforms, and resources in `common`.
4. Verify the held-item render hook against Fabric's renderer and test on a
   Create-only Fabric server before publishing a Fabric JAR.
