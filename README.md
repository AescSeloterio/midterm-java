# Academia One — Clean Project

## First-time setup (each PC)
1. Install JDK 17+ and MySQL Server. VS Code is optional.
2. Open `database/01_create_fresh_database.sql` in MySQL Workbench and execute it.
   This creates `academia_one_fresh` with **empty tables**, without deleting older databases.
3. Open `config/database.properties` and replace `CHANGE_ME` with that PC's MySQL password.
   Never share this filled-in file or commit it to Git.
4. Launch using `run.bat` or VS Code's **Run Academia One** launch configuration.
5. Create a new account from the application's registration screen.

## Notes
- Every PC has its own local database and separate data unless you manually migrate records.
- The original database is not deleted or changed.
- Required images remain under `src/` and `src/avatars/`.
- The bundled MySQL Connector/J is required for `run.bat`.
- `config/database.example.properties` is the safe-to-share template.
- If transferring this project, remove your filled-in `config/database.properties`
  and create a new one from the template on the destination computer.

## Troubleshooting
If login/registration says access denied, check db.user and db.password in config/database.properties. The database name must be academia_one_fresh. The database starts with no accounts; register first.

## Review update and tests
- Registration dialog is scrollable, with visible Create Account and Cancel buttons.
- Login now reports database read errors instead of incorrectly reporting invalid credentials.
- Study Load filters/actions use separate rows to avoid colliding with the title.
- Database credentials remain in `config/database.properties`; the file is excluded from Git.
- Database config defaults to `academia_one_fresh`; changing the local config does not require recompilation.
- VS Code Run and Debug configuration is in `.vscode/launch.json` and uses the project JAR reference from `.vscode/settings.json`.
- Run `test.bat` to check password hashing and registration footer visibility on Windows.

### Important testing limitations
The automated smoke tests do not connect to a live MySQL server. On your computer, verify registration, duplicate email/student ID rejection, login, password change, student data isolation, schedule create/edit/delete and conflicts, grade calculation, tasks persistence, export, and reopening the app. Test on an existing database **after backing it up**. The provided SQL uses `CREATE IF NOT EXISTS`; it does not reset existing tables or delete students.

### Portability
Every computer needs MySQL Server with the same schema. Each local database contains separate records. Do not distribute `config/database.properties` with a real password; use the example template instead.
