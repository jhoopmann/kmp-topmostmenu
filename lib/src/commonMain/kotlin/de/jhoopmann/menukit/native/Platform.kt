package de.jhoopmann.menukit.native

enum class Platform {
    MacOS,
    Windows,
    Linux
}

expect val platform: Platform