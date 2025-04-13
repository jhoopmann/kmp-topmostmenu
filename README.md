# Kotlin Multiplatform Compose Menu Library

## Introduction

Uses kmp-compose-topmostwindow

@TODO

![Menu on desktop space](/doc/img/desktop-space.png)
![Menu on foreign fullscreen spaces.](/doc/img/fullscreen-space.png)

## Compatibility

Tested with

- JDK 21 / 23
- Kotlin Multiplatform 2.1.10
- Jetpack Compose Desktop 1.7.3
- AndroidX-Lifecycle 2.8.0
- macOS 15.3.1, Linux X11, Windows 10/11

## Usage

```
val menuState: MenuState = rememberMenuState()

Menu(
    menuState = menuState,
    layout = {
        /* Custom window layout to apply AppTheme */
        AppTheme { DefaultMenuWindowLayout(it) }
    }
) {
    /* Clickable */
    TextItem(
        text = "Main Window",
        onClick = { ... }
    )

    /* Custom layout */
    TextItem(
        text = "Some Status",
        textLayout = { modifiers, text ->
            Text(
                text = text,
                modifier = modifiers.text.fillMaxWidth(),
                fontSize = 12.sp
            )
        }
    )

    /* Configured modifiers and options */
    TextItem(
        text = "Some Status",
        modifiers = modifiers.apply { text = text.fillMaxWidth() },
        textStyle = TextStyle.Default.copy(fontSize = 12.sp)
    )

    Separator()

    /* Sub Menues */
    SubMenu(
        menuItemLayout = { onGloballyPositioned, onEnter ->
            MenuItem(
                text = "Services",
                iconPainter = Icons.Default.Apps,
                onGloballyPositioned = onGloballyPositioned,
                onEnter = onEnter
            )
        },
        layout = { state, content ->
            AnotherAppTheme {
                DefaultMenuWindowLayout(state, content)
            }
        }
    ) {
            TextItem(text = "Text")
            ...
    }


    Separator()

    IconItem(text = "Quit", iconPainter = Icons.Default.Close, tint = Color.Blue)

    IconItem(text = "Quit", icon { modifiers, painter, tint, text ->
        Icon(
            imageVector = painter,
            tint = Color.Red,
            modifier = modifiers.icon,
            contentDescription = "Extra $text"
        )
    })
}
```

```
menuState.open()
menuState.close()
```

Internal usage UI safe (f.e. customized submenue opening/closing)
```
menuState.eventQueue.trySend {
    if (!synchronized(menuState) { menuState.processing }) {
        menuState.close()
    }
}
```