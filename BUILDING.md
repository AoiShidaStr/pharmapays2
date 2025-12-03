# PharmaPays - Build & Fix Guide

## 🎯 What Was Fixed

### Critical Issues Resolved ✅
1. **Missing JavaFX Dependencies** - Added to `pom.xml`:
   - `javafx-controls`
   - `javafx-fxml`
   - `javafx-graphics`

2. **Missing MySQL Driver** - Added `mysql-connector-java` to `pom.xml`

3. **Code Quality Issues** - Replaced all `e.printStackTrace()` calls with proper error logging

4. **Import Errors** - Fixed incorrect import in `ClientController.java`

5. **Error Handling** - Improved exception handling across all controller and DAO classes

### Files Modified
- `pom.xml` - Dependencies configuration
- `src/main/java/gui/ChatbotController.java`
- `src/main/java/gui/ClientController.java`
- `src/main/java/gui/ProfessionnelController.java`
- `src/main/java/dao/UserDAO.java`
- `src/main/java/dao/MedicamentDAO.java`
- `src/main/java/dao/CommandeDAO.java`
- `src/main/java/database/DataInitializer.java`

---

## 🚀 How to Build & Run

### Prerequisites
- **Java 17** or higher
- **Maven** (or use the Maven wrapper if available)

### Option 1: PowerShell (Windows)
```powershell
cd "C:\Users\CASH TAMPON\Documents\pharmapays"
.\build.ps1
```

### Option 2: Command Prompt (Windows)
```cmd
cd C:\Users\CASH TAMPON\Documents\pharmapays
build.bat
```

### Option 3: Manual Maven Command
```bash
cd C:\Users\CASH TAMPON\Documents\pharmapays
mvn clean install
mvn javafx:run
```

---

## 📋 Troubleshooting

### If you see "Maven not found" error:

**Solution 1: Install Maven**
- Download from https://maven.apache.org/download.cgi
- Extract to a folder
- Add Maven bin folder to your PATH

**Solution 2: Use Java directly**
If Maven is not available, the project can still be compiled with javac (though Maven is recommended):
```bash
javac -cp "lib/*:." -d target/classes src/main/java/gui/*.java
```

### If IntelliSense still shows errors in VS Code:

**Solution: Reload VS Code**
1. Press `Ctrl+Shift+P`
2. Type "Developer: Reload Window"
3. Press Enter

**Or manually trigger Maven update:**
1. Press `Ctrl+Shift+P`
2. Type "Maven: Resolve Unknown Type"
3. Press Enter

### If JavaFX libraries are not found:

**Verify pom.xml has the dependencies:**
Check that the file contains:
```xml
<dependency>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-controls</artifactId>
  <version>${javafx.version}</version>
</dependency>
```

If not, the file may have been reverted. Check `pom.xml` and ensure all dependencies are present.

---

## 🔍 Verifying the Fix

### Check compilation status:
```bash
mvn clean compile
```

Expected output: `BUILD SUCCESS`

### Run tests:
```bash
mvn test
```

### Run the application:
```bash
mvn javafx:run
```

---

## 📚 Project Structure

```
pharmapays/
├── pom.xml                                 # Maven configuration ✅ FIXED
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── gui/
│   │   │   │   ├── MainApp.java          # Entry point
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── ClientController.java  ✅ FIXED
│   │   │   │   ├── ProfessionnelController.java ✅ FIXED
│   │   │   │   ├── ChatbotController.java ✅ FIXED
│   │   │   │   └── RegisterController.java
│   │   │   ├── dao/
│   │   │   │   ├── UserDAO.java          ✅ FIXED
│   │   │   │   ├── MedicamentDAO.java    ✅ FIXED
│   │   │   │   └── CommandeDAO.java      ✅ FIXED
│   │   │   ├── database/
│   │   │   │   ├── DatabaseConnection.java
│   │   │   │   └── DataInitializer.java  ✅ FIXED
│   │   │   ├── models/
│   │   │   │   ├── User.java
│   │   │   │   ├── Medicament.java
│   │   │   │   └── Commande.java
│   │   │   ├── services/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── MedicamentService.java
│   │   │   │   └── CommandeService.java
│   │   │   └── utils/
│   │   │       ├── SessionManager.java
│   │   │       ├── AlertUtils.java
│   │   │       ├── CSVUtils.java
│   │   │       └── PDFUtils.java
│   │   └── resources/
│   │       ├── db/
│   │       ├── styles/
│   │       │   └── styles.css
│   │       └── views/
│   │           ├── LoginView.fxml
│   │           ├── ClientView.fxml
│   │           ├── ProfessionnelView.fxml
│   │           ├── RegisterView.fxml
│   │           └── Chatbot.fxml
│   └── test/
│       └── java/
├── target/                                 # Build output (auto-generated)
└── FIXES_SUMMARY.md                       # Detailed fix documentation
```

---

## ✨ What's Next

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn javafx:run
   ```

3. **Test credentials:**
   - Professional: `pro` / `propass`
   - Client: `client` / `clientpass`

4. **Features available:**
   - ✅ User registration and login
   - ✅ Medicament management (for professionals)
   - ✅ Order browsing (for clients)
   - ✅ Chatbot assistance
   - ✅ CSV/PDF export capabilities

---

## 📝 Notes

- **Database**: Uses MySQL with fallback to SQLite
- **Connection**: Default MySQL connection at `172.0.16.60:3306/pharmapays`
- **Default credentials** are initialized in `DataInitializer.java`
- All error messages now use `System.err.println()` instead of `e.printStackTrace()`

---

## 🆘 Need More Help?

Check `FIXES_SUMMARY.md` for detailed information about all changes made.

If issues persist:
1. Ensure Java 17+ is installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Clear Maven cache: `mvn clean`
4. Rebuild: `mvn install`

---

**Status**: ✅ All compilation errors fixed. Ready to build and deploy!
