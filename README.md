# LAN Sound Packet Disconnect Fix

This repository contains `PacketFixer`, a small fix mod designed to solve a specific bug in Forge (`43.3.7`) modded Minecraft `1.19.2` that causes clients to be disconnected from a LAN world due to a network packet encoding error.

## The Problem

When playing in a modded LAN world, the connecting client (not the host) is suddenly disconnected when entering a specific area or when a specific event occurs. The disconnect message shown to the client is:
```
internal exception io.netty.handler.codec.EncoderException: java.lang.illegalArgumentException: Can't find id for 'net.minecraft.sounds.SoundEvent@...'
```

This error indicates that the server (the host's game) is attempting to send a network packet to the client, telling it to play a sound. However, the server fails to find a registered ID for this sound event in its sound registry. This failure causes a critical error in the `PacketEncoder`, which crashes the network channel for that specific client, resulting in a disconnect. The host player is unaffected because their client and server run in the same process, bypassing this specific network encoding step.

## Root Cause

After debugging, the issue was traced to a vanilla sound event: `minecraft:block.anvil.step`.

This sound is triggered when a mob walks over an **Anvil** block. For an unknown reason within this specific modded environment, this sound event is not correctly registered on the server side of a LAN session. When a mob steps on an anvil, the server tries to create a `ClientboundSoundPacket` for this sound, fails to find its ID, and disconnects any clients who should have received the packet.

## How to Reproduce the Bug

1.  Set up a Minecraft 1.19.2 Forge environment with the mod list specified below.
2.  Start a single-player world on **PC №1 (the Host)**.
3.  Open the world to LAN (`Open to LAN`).
4.  Have **PC №2 (the Client)** join the LAN world.
5.  Place an Anvil block on the ground near the Client player.
6.  Lure or spawn a mob so that it pathfinds and walks across the top surface of the anvil.
7.  **Result:** The Client will be immediately disconnected with the `EncoderException`. The Host will remain in the game without any issue.

## The Solution

The `PacketFixer` mod solves this by using a Mixin to inject code into Minecraft's network `PacketEncoder`.

Instead of letting the packet encoder crash, this mod performs a proactive check:
1.  It intercepts any outgoing sound packet (`ClientboundSoundPacket`).
2.  It safely checks if the `SoundEvent` contained within the packet is registered in the game's sound registry.
3.  If the sound is **not registered** (which is the case for `minecraft:block.anvil.step` in this scenario), the mod logs a detailed warning to the host's console and **discards the packet entirely**.
4.  The original game code that would have crashed is never executed.

This prevents the client from being disconnected, with the only side effect being that the client will not hear the "broken" sound.

## Mod Environment

This issue was discovered and reproduced in a Minecraft 1.19.2 environment using Forge `43.3.7` with the following mods installed:

| Mod Name | Version |
| :--- | :--- |
| Advancement Plaques | 1.4.7 |
| Alloyed | 1.5a |
| AppleSkin | 2.4.2 |
| Architectury API | 6.5.85 |
| AutoRegLib | 1.8.2-55 |
| Balm | 4.6.0 |
| Better Advancements | 0.3.0.148 |
| Better Chunk Loading | 2.5 |
| Better Statistics Screen | 2.2.2 |
| Better Village | 3.2.0 |
| Bookshelf | 16.3.20 |
| Canary | 0.3.2 |
| Catalogue | 1.7.0 |
| CC: Tweaked | 1.101.4 |
| CCCBridge | 1.5.1 |
| Chunk Sending | 2.8 |
| CIT Reforged | 1.19 |
| Clockwork | 0.1.2 |
| Clumps | 9.0.0+14 |
| Configured | 2.1.1 |
| Connected Glass | 1.1.11 |
| Controlling | 10.0+7 |
| Copycats | 1.3.8 |
| Create | 0.5.1.f |
| Create Confectionery | 1.0.9 |
| Create Diesel 'n' Desire | 0.1c |
| Create Stuff & Additions | 2.0.4a |
| Create: Central Kitchen | 1.3.9.g |
| Create: Crystal Clear | 0.2.1 |
| Create: Deco Casing | 3.1.0 |
| Create: Enchantment Industry | 1.2.7.f |
| Create: Interactive | 1.0.1-beta.2 |
| Create: Misc and Things | 4.0A |
| Create: New Age | 1.2.2 |
| Create: Ore Excavation | 1.2.2 |
| Create: The Factory Must Grow | 0.5.3.b |
| CreateCasing | 1.5.0-ht3 |
| CreateDeco | 1.3.3-1.19.2 |
| CreateDieselGenerators | 1.2f |
| CreateEnderTransmission | 2.0.4 |
| CreateGoggles | 0.5.5.f |
| Cupboard | 2.3 |
| Curios API | 5.1.6.3 |
| Decoration Delight Refurbished | 1.19.2 |
| Decorative Blocks | 3.0.0 |
| Design Decor | 0.3 |
| Detail Armor Bar | 2.6.4 |
| Diagonal Fences | 4.2.6 |
| Diagonal Windows | 4.0.2 |
| Do a Barrel Roll | 2.6.2 |
| DrawerFPS | 1.8 |
| Dummmmmmy | 1.7.1 |
| Dynamic Lights Reforged | 1.4.0 |
| Easy Anvils | 4.0.11 |
| EmoteCraft | 2.2.7-b |
| End's Delight | 1.2.1 |
| Entity Culling | 1.6.1 |
| Entity Model Features | 2.0.2 |
| Entity Texture Features | 6.0.1 |
| Eureka! | 1.2.0-beta.2 |
| Extended Cogwheels | 2.1.0 |
| FancyMenu | 2.14.13 |
| Farmer's Delight | 1.2.4 |
| FerriteCore | 5.0.3 |
| FPS Reducer | 2.1 |
| Framework | 0.6.16 |
| Friendly Fire | 14.0.6 |
| Fusion | 1.1.0c |
| Global Packs | 1.14.5 |
| GPU Memory Leak Fix | 1.6 |
| Gravestone Mod | 1.0.10 |
| Iceberg | 1.1.4 |
| Integrated Dungeons and Structures | 1.7.7 |
| Immersive Paintings | 0.6.7 |
| Integrated API | 1.2.8 |
| Integrated Stronghold | 1.0.2 |
| Interiors | 0.5.2 |
| Italian Delight | 1.5 |
| Jade | 8.9.1 |
| Jade Addons | 3.6.0 |
| Just Enough Items (JEI) | 11.6.0.1018 |
| Konkrete | 1.8.0 |
| Kotlin for Forge | 3.12.0 |
| LazyDFU | 1.0.2 |
| Legendary Tooltips | 1.4.0 |
| Library Ferret | 4.0.0 |
| Lightspeed | 1.0.5 |
| Load My Resources | 1.0.4 |
| Lootr | 0.4.25.65 |
| Make Bubbles Pop | 0.2.0 |
| Macaw's Bridges | 3.0.1 |
| ModernFix | 5.12.0 |
| Moonlight Library | 2.3.6 |
| More Red | 3.0.0.2 |
| MoreRed CCT Compat | 1.0.0 |
| Mouse Tweaks | 2.23 |
| Nether's Delight | 3.1 |
| No More Potion Sickness | 1.1 |
| No Chat Reports | 1.5.1 |
| Not Enough Crashes | 5.0.0 |
| Ocean's Delight | 1.0.2 |
| OctoLib | 0.3 |
| Oculus | 1.6.9 |
| Oculus-Flywheel Compat | 0.2.1 |
| Pick Up Notifier | 4.2.4 |
| Player Animation Library | 1.0.1 |
| Polymorph | 0.46.5 |
| Prism | 1.0.2 |
| Procedural RenderManager API | 0.2.5-alpha |
| Puzzles Lib | 4.4.3 |
| Quality Crops | 1.3.3 |
| Quality's Delight | 1.5.3 |
| Quark | 3.4-418 |
| Reblured | 1.2.0 |
| Rechiseled | 1.1.5c |
| Rechiseled: Create | 1.0.1 |
| Relics | 0.6.2.4 |
| Rubidium | 0.6.2c |
| Serene Seasons | 8.1.0.24 |
| 3D Skin Layers | 1.6.6 |
| Slice and Dice | 2.3.3 |
| Smooth Chunk | 3.5 |
| Sophisticated Backpacks | 3.19.5.989 |
| Sophisticated Core | 0.5.111.525 |
| Sound Physics Remastered | 1.4.5 |
| Spawner Mod | 1.9.1 |
| Starlight | 1.1.1 |
| Steam 'n' Rails | 1.5.3 |
| Storage Drawers | 11.1.2 |
| SuperMartijn642's Config Lib | 1.1.8 |
| SuperMartijn642's Core Lib | 1.1.16 |
| Supplementaries | 2.4.18 |
| Timeless and Classics - Guns | 1.0.2 |
| Terralith | 2.3.12 |
| TL_SKIN_CAPE | 1.30 |
| Tough As Nails | 8.0.0.78 |
| Tough As Tweaked | 1.0.2 |
| Valkyrien Skies | 2.1.2-beta.1 |
| Waystones | 11.4.2 |
| Xaero Arrow Fix | 1.1 |
| Xaero's World Map | 1.37.7 |
| Xaero's Minimap | 23.9.7 |
| Yeetus Experimentus | 1.0.1 |
| YUNG's API | 3.8.10 |
| YUNG's Better Desert Temples | 2.2.2 |
| YUNG's Better Dungeons | 3.2.2 |
| YUNG's Better End Island | 1.0 |
| YUNG's Better Jungle Temples | 1.0.1 |
| YUNG's Better Mineshafts | 3.2.1 |
| YUNG's Better Nether Fortresses| 1.0.6 |
| YUNG's Better Ocean Monuments | 2.1.1 |
| YUNG's Better Strongholds | 3.2.0 |