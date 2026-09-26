# CodeCanvas - Interactive CS Algorithm Visualizer (Architecture 2.0)

A production-ready JavaFX 17+/21 multi-screen desktop application built for computer science students and instructors. Features modern flat aesthetic styling (`-fx-background-radius: 8`, soft shadows), a SINGLE unified header bar with a consolidated profile chip, Role-Based Access Control (Admin vs. Student), an interactive 20-Question MCQ Quiz Module with automated rank promotions, SQLite foreign-key persistence, asynchronous GitHub REST API fetching, polymorphic algorithm visualizer engines, and local simulation video playback with graceful missing-file fallbacks.

---

## Key System Architecture & Academic Rubric Conformance

1. **Single Unified Header Bar & Modern Design Language:**
   - Single unified top bar eliminating duplicate profile buttons.
   - Consolidated User Profile Chip displaying avatar, user full name (e.g. `👤 Tasnia Rahman`), and level badge (`[Intermediate]`).
   - Clean modern CSS (`style.css`): rounded cards (`-fx-background-radius: 8`), subtle borders, soft elevation, borderless inputs, styled `TableView`/`TreeView`, and 4 themes: `Light Blue`, `Dark Slate`, `Emerald Green`, `Crimson Red`.

2. **Role-Based Access Control (RBAC):**
   - **Hardcoded Administrator:**
     - **Username:** `Admin123_`
     - **Password:** `Admin123_`
     - **Role:** `"ADMIN"`
     - **Privileges:** Grants access to the "Add Student" form alongside full student CRUD capabilities (`PersonDao` / `TableView<Person>`).
   - **Student Mode:**
     - **Default Account:** `student` / `Student123!` (Tasnia Rahman)
     - **Role:** `"STUDENT"`
     - **Privileges:** Read-only class directory. "Add Student", update, and delete controls are completely hidden/disabled.
     - **Peer Directory Inspection:** Students can browse classmates, click on peers, and view read-only stats (Level Badge, Total Correct MCQs, Overall Score, Saved Algorithms Count) without modification access.

3. **Advanced OOP & Design Patterns:**
   - **Generic DAO Pattern:** `Dao<T, ID>` interface (`findById`, `findAll`, `insert`, `update`, `delete`) implemented by:
     - `UserDao implements Dao<User, String>`
     - `PersonDao implements Dao<Person, Integer>`
   - **Polymorphic Visualizers:** Abstract class `AlgorithmVisualizer` with abstract method `runSimulation()`, subclassed by:
     - `GraphAlgorithmVisualizer` (simulates BFS, DFS, Dijkstra with vertex queues and edge relaxations)
     - `SortingAlgorithmVisualizer` (simulates Quick Sort, Merge Sort with comparisons and swaps)

4. **Dynamic Window Property Bindings:**
   - Explicit responsive scaling in controllers (e.g. `root.widthProperty().divide(...)` and `profileImageView.fitWidthProperty().bind(...)`) for responsive UI scaling.

5. **Concurrency Management:**
   - `AppExecutor.java` manages a fixed thread pool of 4 worker threads (`Executors.newFixedThreadPool(4)`) for all GitHub API requests, background DB writes, and async operations.
   - Clean termination via `AppExecutor.shutdown()` executed inside `Application.stop()`.

6. **SQLite Foreign Keys:**
   - `PRAGMA foreign_keys = ON;` executed on every database connection.
   - `FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE` on `saved_items`.

7. **MCQ Quiz Module & Automated Level Promotions:**
   - 20 questions parsed from JSON per algorithm topic (BFS, DFS, Dijkstra, Bellman-Ford, Floyd-Warshall, Johnson's, Quick Sort, Merge Sort).
   - Radio choices with instant grading, correct answer reveals, and comprehensive explanations.
   - Asynchronously increments user's `total_correct_mcqs` in SQLite.
   - **Automated Level Promotion:** Automatically promotes ranks (`Beginner` < 20, `Intermediate` 20..99, `Expert` >= 100) and displays a celebratory Alert Dialog!

---

## Screen & Feature Flow Matrix (Figures 1–7 + MCQ + RBAC)

| Screen | Controller | View (FXML) | Features Implemented |
| :--- | :--- | :--- | :--- |
| **Figure 1: Authentication System** | `AuthController.java` | `auth_view.fxml` | Dual Mode (Login & Sign-Up), real-time password requirement checklist indicators, password reveal toggle (`Show`/`Hide`), SQLite credentials check and account creation with default `STUDENT` role. |
| **Figure 2: Algorithm Dashboard** | `DashboardController.java` | `dashboard_view.fxml` | Real-time case-insensitive search filter across algorithm names and summaries; Category `TreeView`; `TableView` catalog; double-click navigation to Detail Hub; Polymorphic "Run Simulation Trace" benchmark trigger. |
| **Figures 3 & 4 + MCQ Section: Detail Hub** | `AlgorithmDetailController.java` | `algorithm_detail_view.fxml` | 4 core view triggers: `[Algorithm]`, `[Code]`, `[Simulator]`, `[MCQ Quiz]`. Asynchronous GitHub REST API fetch via `AppExecutor`. Actions: "Copy Code" to clipboard and "Save" bookmark to SQLite. Local file check in `videos123/{AlgorithmName}.mp4` with playback controls or `"Simulation video not available"` fallback. 20-question MCQ quiz with instant grading, explanations, and level promotion alerts. |
| **Figure 5: Profile & Student Directory** | `ProfileController.java` | `profile_view.fxml` | Avatar image via `ImageView` & `FileChooser` with dynamic property width binding; profile details display; RBAC banner; Admin Mode with full Add/Update/Delete student form vs Student Mode read-only; Class Peer Directory with read-only progress cards; Integrated Course UI Components Matrix. |
| **Figures 6 & 7: Saved Bookmarks Hub** | `SavedHubController.java` | `saved_hub_view.fxml` | Filter tabs: `[All]`, `[Algorithm]`, `[Code]`, `[Simulator]`. `TableView` displaying user-saved SQLite items. Detail inspection panel showing saved content. "Open in Detail Hub" and "Delete Bookmark" actions. |

---

## Default Accounts

| Role | Username | Password | Full Name | Default Rank | Privileges |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Administrator** | `Admin123_` | `Admin123_` | System Administrator | Expert | Full student management (Add/Update/Delete) + All app features |
| **Student** | `student` | `Student123!` | Tasnia Rahman | Intermediate | Read-only student directory, peer inspection, MCQs, bookmarks |

---

## How to Run

```bash
mvn clean javafx:run
```

All SQLite tables (`codecanvas.db`) with foreign keys and the simulation directory (`videos123/`) are initialized automatically on startup.
