# Shared Sources

Both loader projects compile these sources against their own Minecraft environment.
No separate core mod or runtime library is required.

- `src/main`: gameplay, mixins, commands, JSON config, permission policy and update checker.
- `src/client`: optional Cloth Config screen, loaded through the appropriate client integration.
- `src/test`: regression tests executed by both loader builds.

Only `fabric` and `neoforge` may depend on loader-specific APIs. `LoaderPlatform`
provides the small set of loader operations used by the shared code.
