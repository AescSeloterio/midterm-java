# Academia One — Java Swing UI refinement

This package is the original Java desktop application, **not** a React conversion. The uploaded Figma/React website was used as the visual reference.

## Changes in this revision
- Updated the core page and sidebar greens to better match the modern website palette.
- Replaced the duplicate Study Load book icon with a dedicated three-line list icon (including the dashboard shortcut).
- Dashboard now starts in a normal resizable window rather than forcing maximized mode. Students may maximize it themselves.
- Dashboard statistics and content cards adapt to available viewport width (four/two/one stat columns; two/one content columns).
- Header controls move below the greeting in narrow windows; dashboard vertical scrolling is smoother.
- Existing MySQL access, account logic, curriculum, schedules, tasks, announcements and other Java functionality remain in place.

## Limits and testing
- Java source compiled successfully with JDK 21.
- This is an incremental desktop UI update, **not** a complete pixel-perfect Figma implementation. Other pages still have legacy layout styling.
- MySQL connection and end-to-end GUI testing require a configured Windows machine and database.
- Local `config/database.properties` was excluded from this distributable to avoid shipping machine-specific database credentials. Copy `config/database.example.properties` to `config/database.properties` and set credentials on your computer.

## Running on Windows
1. Extract the ZIP.
2. Ensure Java JDK 17+ and MySQL Server are installed and the existing Academia One database is available.
3. Configure `config/database.properties` for your local database.
4. Double-click `run.bat`.
