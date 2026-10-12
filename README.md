# 奔流 honryū — Torrents

A BitTorrent client for an E Ink phone. Black on white, Lato, nothing that slides.

*Honryū* is a torrent in the old sense: a rushing stream.

Built for the [Mudita Kompakt](https://mudita.com/products/kompakt/), whose 4.3" panel has
sixteen greys and a slow redraw.

## What it is

Yaroslav Pronin's [LibreTorrent](https://gitlab.com/proninyaroslav/libretorrent), dressed
for E Ink. Everything that makes it a good client is his and unchanged: magnet links and
`.torrent` files, choosing which files to fetch, sequential download, RSS feeds, a scheduler,
speed limits, a proxy with a kill switch, and making torrents of your own.

What is mine is how it looks on this screen.

## What changed for E Ink

**One appearance.** The light, dark, black and dynamic-colour themes are gone. The panel can
only show dark marks on a light ground, and a theme setting it cannot honour is a setting
that lies.

**Black and white only.** Every colour in the theme is black or white, so nothing arrives as
a muddy mid-grey. Status, progress and choices are carried by shape and words.

**Mudita's type and components.** Lato at [MMD](https://github.com/mudita/MMD)'s sizes;
switches, chips, progress bars, menus, dialogs and the bottom bar drawn the way MMD draws
them; settings rows ruled in black, without icons.

**No shadows, ripples, dimming or slides.** On E Ink each of those is a smear. Dialogs and
menus carry a black rim instead, and pages change at once.

**Back closes the side drawer** before it leaves the page.

## Getting it, and keeping it

Download <https://github.com/wanderwildwood/honryu/releases/latest/download/honryu.apk> and
sideload it. That address always points at the newest release, and every release publishes a
`.sha256` beside the APK.

For updates, add this repository to [Obtainium](https://github.com/ImranR98/Obtainium):

    https://github.com/wanderwildwood/honryu

**The application id is settled** — updates install over what you have, keeping your
torrents. It installs beside LibreTorrent rather than over it.

## Building

```
./gradlew assembleBaseRelease
```

A release is signed by a keystore in `signing/`, which is not in this repository. Without it
the release APK builds **unsigned** and will not install anywhere — there is no fallback key
by design.

## Privacy

See [PRIVACY.md](PRIVACY.md). In short: the other people in a torrent see your internet
address, and nothing else is sent anywhere.

## Licence

**GNU General Public License v3.0 or later.** See [LICENSE.md](LICENSE.md).

This is *or later*, not the GPL-3.0-only these apps otherwise use. LibreTorrent is published
under "version 3 of the License, or (at your option) any later version", and that option
was granted to everyone downstream. It is not mine to take away.

libtorrent is © Arvid Norberg, BSD; libtorrent4j is © Alden Torres, MIT. Lato is © Łukasz
Dziedzic, SIL Open Font License 1.1.

Copyright © wander wildwood, and © 2016–2026 Yaroslav Pronin for the parts that are his.
