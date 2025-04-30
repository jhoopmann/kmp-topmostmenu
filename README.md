# Kotlin Multiplatform Compose Menu Library

## Introduction [1.2.1]

Full customizable menu for usage as in-app context menu or tray icon for example.
Uses ```kmp-compose-stickywindow``` and ```kmp-stickywindow``` to be able to be displayed sticky (also on foreign fullscreen spaces on macOS). Autoclose on focus lost.

![Menu on desktop space](/doc/img/desktop-space.png)
![Menu on foreign fullscreen spaces.](/doc/img/fullscreen-space.png)
![Custom Icon Item Composable](/doc/img/custom-icon-item.png)

## Compatibility

Tested with

- JDK 21 / 23
- Kotlin Multiplatform 2.1.10
- Jetpack Compose Desktop 1.7.3
- AndroidX-Lifecycle 2.8.0
- macOS 15.3.1, Linux X11, Windows 10/11

## Usage

```
val menuState: MenuState = rememberMenuWindowState()

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
        state = rememberMenuWindowState(),
        menuItemLayout = { onGloballyPositioned, onEnter ->
            MenuItem(
                text = "Services",
                iconPainter = Icons.Default.Apps,
                onGloballyPositioned = onGloballyPositioned,
                onEnter = onEnter
            )
        },
        layout = {
            AnotherAppTheme {
                DefaultMenuWindowLayout(it)
            }
        }
    ) {
            TextItem(text = "Text")

            SubMenu(
                state = rememberMenuWindowState(),
                menuItemLayout = { onGloballyPositioned, onEnter ->
                   ...
                }
            ) { ... }
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

You can use BaseItem to implement whatever layout you want, just ignore the prepend / append and center methods.
Example:
![Custom Icon Item Composable](/doc/img/custom-icon-item.png)

```
@Composable
fun MenuState.CustomImageItem() {
    BaseItem(modifiers = ItemModifiers()) { modifiers, prepend, append, center ->
        Row(modifier = modifiers.item.padding(20.dp), horizontalArrangement = Arrangement.Center) {
            Column {
                Icon(Icons.Default.Abc, modifier = Modifier.size(400.dp), contentDescription = "TestIcon")
            }
        }
    }
}

```

Or declare your own ItemLayout, default is Row->Row
```
@Composable
fun MenuState.CustomItem(customText: Text, layout: ItemLayout = { modifiers, prepend, append, content ->
        Column(modifier = modifiers.item) {
            Row(modifiers.contents.prepend.wrapContentHeight()) {
                ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Column }) {
                    prepend {
                        ... any content ...
                    }
                }
            }
            Row(modifiers.contents.center.wrapContentHeight()) {
                ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Column }) {
                    center {
                        ... any content ...
                    }
                }
            }
            Row(modifiers.contents.append.wrapContentHeight()) {
                ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Column }) {
                    appemdm {
                        ... any content ...
                    }
                }
            }
        }
    }
) {
    BaseItem() { modifiers, prepend, append, center ->
        layout(
            modifiers,
            prepend,
            append
            { it ->
                center {
                    Text(customText)
                }
            }
        )
    }
}
```

Emit menu action to close or open. Actions are debounced (32ms) collected in composable coroutine scope on the main dispatcher.

```
menuState.emitOpen()
menuState.emitClose()
```

Pass AwtWindow onPreviewKey or onKey KeyEvents to menuState.handleKeyEvent for keyboard shortcuts 
```
menuState.handleKeyEvent(event: KeyEvent)
```