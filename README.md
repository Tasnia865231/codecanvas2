# CodeCanvas - Interactive Algorithm Visualizer for CS Students

A production-ready JavaFX 17+/21 multi-screen desktop application built for computer science students and educators. Features an end-to-end architecture adhering to the multi-screen navigation wireframes (Figures 1–7), SQLite persistence, GitHub REST API dynamic code and documentation fetching, and local video simulation playback with graceful missing-file fallback handling.

---

## Technical Stack & System Architecture

- **GUI Framework:** JavaFX 17+ (Controls, FXML, CSS, Media)
- **Persistence:** SQLite via JDBC (`codecanvas.db`), with tables:
  - `users` (credentials with SHA-256 + salt hashing, student profile attributes)
  - `saved_items` (user-bookmarked algorithms, code implementations, and simulator references)
  - `person` (course students database backing TableView CRUD)
  - `execution_trace` (algorithm trace comparisons and API sync)
- **Remote Integration:** Java 11+ Native `HttpClient` fetching JSON payloads from GitHub REST API (`https://api.github.com/repos/{owner}/{repo}/contents/{path}`) with Base64 content decoding and built-in offline repository fallbacks.
- **Media Engine:** `javafx.scene.media` (`Media`, `MediaPlayer`, `MediaView`) with dynamic existence validation against `videos123/{AlgorithmName}.mp4`.
- **Navigation:** Centralized `NavigationManager` managing a back-button stack (`Stack<NavigationEntry>`), application theme switching ("Red", "Green", "Blue"), and view transitions across Figures 1–7.

---

## Screen & Feature Flow Matrix (Figures 1–7)

### 1. Authentication System (Figure 1)
- **Dual Mode:** Tabbed interface supporting **Sign In** and **Create Account**.
- **Sign-Up Password Validation:** Enforces strong password criteria in real-time with visual indicators:
  - Minimum 8 characters
  - At least one uppercase letter (`[A-Z]`)
  - At least one lowercase letter (`[a-z]`)
  - At least one numeric digit (`[0-9]`)
  - At least one special symbol (`[!@#$%^&*...]`)
- **Password Reveal:** Show/Hide toggle button revealing plain text.
- **Persistence:** Credentials persisted in SQLite with cryptographic salting and SHA-256 hashing.
- **Default Demo Account:** `student` / `Student123!`

### 2. Main Algorithm Dashboard & Search (Figure 2)
- **Search Bar:** Real-time, case-insensitive filter across algorithm name, category, and summary descriptions.
- **Category TreeView:** Hierarchical navigation (`Graph Theory`, `Dynamic Programming`, `Divide & Conquer`, `Searching`).
- **Algorithm Catalog TableView:** Lists BFS, DFS, Dijkstra, Bellman-Ford, Floyd-Warshall, Johnson's, Quick Sort, Merge Sort, Binary Search with complexities.
- **Navigation:** Double-clicking or selecting any row transitions directly to the Detail Hub (Figure 3).
- **Profile Header:** Direct shortcut to Student Profile & Lab (Figure 5).

### 3. Algorithm Detail Hub & Resources (Figures 3 & 4)
- **View Triggers:** Three core navigation triggers:
  - `[ Algorithm ]`: Algorithm description, characteristics, and pseudo-code dynamically fetched from GitHub REST API.
  - `[ Code ]`: Clean Java implementation fetched from GitHub REST API.
  - `[ Simulator ]`: Interactive simulation video player.
- **Action Buttons:**
  - **Copy Code:** Copies active Java code to system clipboard (`javafx.scene.input.Clipboard`) with visual feedback.
  - **Save Bookmark:** Persists current view (Algorithm, Code, or Simulator reference) into the user's SQLite profile.
- **Simulator & Video Fallback Engine:**
  - Checks if `videos123/{AlgorithmName}.mp4` exists on local disk.
  - **If Found:** Loads into `MediaView` with full playback controls:
    - Play / Pause buttons
    - Progress Scrubber `Slider` synchronized with `MediaPlayer.currentTimeProperty()`
    - Timestamp label (`00:15 / 02:40`)
    - Volume `Slider` (0%–100%)
  - **If Missing (e.g., Floyd-Warshall, Johnson's):** Gracefully hides `MediaView` and displays:
    `"Simulation video not available"` along with instructions to place MP4 files into `videos123/`.

### 4. User Profile & Saved Bookmark Hub (Figures 5, 6 & 7)
- **Profile View (Figure 5):**
  - Avatar `ImageView` with "Browse..." (`FileChooser`) and "Reset" buttons.
  - User details display and profile edit persistence in SQLite.
  - Direct navigation button to **Saved Bookmarks Hub** (Figure 6).
- **Integrated Course UI Components Matrix:**
  - `Initializable` controllers across all views.
  - `TableView<Person>` with `Person` model (id, name, level, dob) and custom `dd-MMM-yyyy` date formatting + Add/Update/Delete actions.
  - `RadioButton` & `ToggleGroup` for Skill Level selector ("Beginner", "Intermediate", "Expert") updating a Label.
  - `CheckBox` selector for Hobbies (Reading, Gaming, Traveling) with a "Submit" action button.
  - `ChoiceBox` for Application Theme ("Red", "Green", "Blue") dynamically altering CSS root classes.
  - `ComboBox` for Country selection.
  - `DatePicker` for Date of Birth with custom formatting (`dd-MMM-yyyy`).
  - `ColorPicker` modifying target Label text fill color dynamically.
  - `ListView` & `TreeView` with item click listeners updating display labels.
  - `ProgressBar` + `TextField` accumulator: user enters $N$; "Accumulate" button increments progress ($count / N$) until $1.0$.
  - `Slider` (Font Size 10pt–36pt) and numeric `Spinner` (1–10) with reactive display labels.
  - `Alert` dialog triggers: Information, Warning, and Error dialogs.
  - `TextArea` with "Clear" action; `PasswordField` with reveal toggle.
  - Code-based programmatic event wiring (`setOnAction` and Enter-key press).
- **Saved Items Hub (Figures 6 & 7):**
  - Filter tabs: `[ All ]`, `[ Algorithm ]`, `[ Code ]`, `[ Simulator ]`.
  - Itemized `TableView` of user-saved bookmarks from SQLite.
  - Detail inspector displaying saved content.
  - "Open in Detail Hub" and "Delete Bookmark" actions.

---

## How to Run

### Requirements
- JDK 17 or newer (JDK 21/26 recommended)
- Maven 3.8+

### Execution Command

```bash
mvn clean javafx:run
```

All SQLite tables (`codecanvas.db`) and the simulation directory (`videos123/`) are initialized automatically on startup.
