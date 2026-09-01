package com.buildstudio.ide.generator;

import com.buildstudio.ide.model.Project;
import com.buildstudio.ide.model.TemplateType;
import com.buildstudio.ide.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class ProjectGenerator {

    public static void generateProject(Project project) throws IOException {
        File rootDir = project.getRootDir();
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        // 1. settings.gradle
        File settingsGradle = new File(rootDir, "settings.gradle");
        String settingsContent = "dependencyResolutionManagement {\n" +
                "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)\n" +
                "    repositories {\n" +
                "        google()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "}\n" +
                "rootProject.name = \"" + project.getName() + "\"\n" +
                "include ':app'\n";
        FileUtils.writeStringToFile(settingsGradle, settingsContent);

        // 2. build.gradle (Root)
        File rootBuildGradle = new File(rootDir, "build.gradle");
        String rootBuildContent = "// Top-level build file for " + project.getName() + "\n" +
                "buildscript {\n" +
                "    repositories {\n" +
                "        google()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "    dependencies {\n" +
                "        classpath 'com.android.tools.build:gradle:8.2.2'\n" +
                "    }\n" +
                "}\n\n" +
                "allprojects {\n" +
                "    repositories {\n" +
                "        google()\n" +
                "        mavenCentral()\n" +
                "    }\n" +
                "}\n\n" +
                "tasks.register('clean', Delete) {\n" +
                "    delete rootProject.buildDir\n" +
                "}\n";
        FileUtils.writeStringToFile(rootBuildGradle, rootBuildContent);

        // 3. app/build.gradle
        File appBuildGradle = new File(project.getAppDir(), "build.gradle");
        String appBuildContent = "plugins {\n" +
                "    id 'com.android.application'\n" +
                "}\n\n" +
                "android {\n" +
                "    namespace '" + project.getPackageName() + "'\n" +
                "    compileSdk " + project.getTargetSdk() + "\n\n" +
                "    defaultConfig {\n" +
                "        applicationId \"" + project.getPackageName() + "\"\n" +
                "        minSdk " + project.getMinSdk() + "\n" +
                "        targetSdk " + project.getTargetSdk() + "\n" +
                "        versionCode 1\n" +
                "        versionName \"1.0.0\"\n" +
                "    }\n\n" +
                "    buildTypes {\n" +
                "        release {\n" +
                "            minifyEnabled false\n" +
                "        }\n" +
                "    }\n" +
                "    compileOptions {\n" +
                "        sourceCompatibility JavaVersion.VERSION_1_8\n" +
                "        targetCompatibility JavaVersion.VERSION_1_8\n" +
                "    }\n" +
                "}\n\n" +
                "dependencies {\n" +
                "    implementation fileTree(dir: 'libs', include: ['*.jar'])\n" +
                "}\n";
        FileUtils.writeStringToFile(appBuildGradle, appBuildContent);

        // 4. editorOpened.json (BUILD STUDIO workspace state)
        File editorOpenedJson = new File(rootDir, "editorOpened.json");
        String mainJavaPath = project.getMainActivityFile().getAbsolutePath();
        String editorJson = "[{\"path\":\"" + mainJavaPath + "\"}]\n";
        FileUtils.writeStringToFile(editorOpenedJson, editorJson);

        // 5. AndroidManifest.xml
        File manifestFile = project.getManifestFile();
        String manifestContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"" + project.getPackageName() + "\"\n" +
                "    android:versionCode=\"1\"\n" +
                "    android:versionName=\"1.0\">\n" +
                "    <uses-sdk android:minSdkVersion=\"" + project.getMinSdk() + "\" android:targetSdkVersion=\"" + project.getTargetSdk() + "\" />\n\n" +
                "    <application\n" +
                "        android:allowBackup=\"true\"\n" +
                "        android:icon=\"@drawable/app_icon\"\n" +
                "        android:roundIcon=\"@drawable/app_icon\"\n" +
                "        android:label=\"@string/app_name\"\n" +
                "        android:theme=\"@android:style/Theme.Material.Light.NoActionBar\">\n" +
                "        <activity\n" +
                "            android:name=\".MainActivity\"\n" +
                "            android:exported=\"true\">\n" +
                "            <intent-filter>\n" +
                "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                "            </intent-filter>\n" +
                "        </activity>\n" +
                "    </application>\n\n" +
                "</manifest>\n";
        FileUtils.writeStringToFile(manifestFile, manifestContent);

        // 6. Resources directories
        File resDir = new File(project.getAppDir(), "src/main/res");
        File valuesDir = new File(resDir, "values");
        File layoutDir = new File(resDir, "layout");
        File drawableDir = new File(resDir, "drawable");
        valuesDir.mkdirs();
        layoutDir.mkdirs();
        drawableDir.mkdirs();

        // 6b. Generated project launcher icon.
        String iconXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:width=\"48dp\" android:height=\"48dp\"\n" +
                "    android:viewportWidth=\"24\" android:viewportHeight=\"24\">\n" +
                "    <path android:fillColor=\"#534AEF\" android:pathData=\"M6,18c0,0.55 0.45,1 1,1h1v3.5c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5V19h2v3.5c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5V19h1c0.55,0 1,-0.45 1,-1V8H6v10zM3.5,8C2.67,8 2,8.67 2,9.5v7c0,0.83 0.67,1.5 1.5,1.5S5,17.33 5,16.5v-7C5,8.67 4.33,8 3.5,8zm17,0c-0.83,0 -1.5,0.67 -1.5,1.5v7c0,0.83 0.67,1.5 1.5,1.5s1.5,-0.67 1.5,-1.5v-7c0,-0.83 -0.67,-1.5 -1.5,-1.5z\" />\n" +
                "</vector>\n";
        FileUtils.writeStringToFile(new File(drawableDir, "app_icon.xml"), iconXml);

        // 7. Base values (strings.xml, colors.xml, styles.xml)
        String stringsXml = "<resources>\n" +
                "    <string name=\"app_name\">" + project.getName() + "</string>\n" +
                "    <string name=\"welcome_msg\">Welcome to " + project.getName() + "!</string>\n" +
                "    <string name=\"btn_click_me\">Tap Here</string>\n" +
                "    <string name=\"count_label\">Count: 0</string>\n" +
                "</resources>\n";
        FileUtils.writeStringToFile(new File(valuesDir, "strings.xml"), stringsXml);

        String colorsXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <color name=\"primary\">#534AEF</color>\n" +
                "    <color name=\"primary_dark\">#4036E0</color>\n" +
                "    <color name=\"accent\">#00B894</color>\n" +
                "    <color name=\"bg_light\">#F8F9FD</color>\n" +
                "    <color name=\"text_dark\">#111827</color>\n" +
                "</resources>\n";
        FileUtils.writeStringToFile(new File(valuesDir, "colors.xml"), colorsXml);

        // 8. Generate Specific Code for Template Type
        File javaDir = project.getMainJavaDir();
        javaDir.mkdirs();

        generateSpecificTemplate(project, javaDir, layoutDir);
    }

    private static void generateSpecificTemplate(Project project, File javaDir, File layoutDir) throws IOException {
        TemplateType type = project.getTemplateType();
        if (type == null) type = TemplateType.NAV_DRAWER;

        switch (type) {
            case SIMPLE_APP:
                generateSimpleAppTemplate(project, javaDir, layoutDir);
                break;
            case FAB_APP:
                generateFabAppTemplate(project, javaDir, layoutDir);
                break;
            case FULLSCREEN_APP:
                generateFullscreenAppTemplate(project, javaDir, layoutDir);
                break;
            case NAV_DRAWER:
            default:
                generateNavDrawerTemplate(project, javaDir, layoutDir);
                break;
        }
    }

    // ==========================================
    // 1. SIMPLE APP TEMPLATE
    // ==========================================
    private static void generateSimpleAppTemplate(Project project, File javaDir, File layoutDir) throws IOException {
        String javaCode = "package " + project.getPackageName() + ";\n\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "import android.view.View;\n" +
                "import android.widget.Button;\n" +
                "import android.widget.TextView;\n" +
                "import android.widget.Toast;\n\n" +
                "public class MainActivity extends Activity {\n\n" +
                "    private int counter = 0;\n" +
                "    private TextView tvTitle;\n" +
                "    private TextView tvCounter;\n" +
                "    private Button btnIncrement;\n" +
                "    private Button btnReset;\n\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.main);\n\n" +
                "        tvTitle = findViewById(R.id.tv_title);\n" +
                "        tvCounter = findViewById(R.id.tv_counter);\n" +
                "        btnIncrement = findViewById(R.id.btn_increment);\n" +
                "        btnReset = findViewById(R.id.btn_reset);\n\n" +
                "        btnIncrement.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                counter++;\n" +
                "                tvCounter.setText(\"Clicks: \" + counter);\n" +
                "                Toast.makeText(MainActivity.this, \"Counter updated!\", Toast.LENGTH_SHORT).show();\n" +
                "            }\n" +
                "        });\n\n" +
                "        btnReset.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                counter = 0;\n" +
                "                tvCounter.setText(\"Clicks: 0\");\n" +
                "                Toast.makeText(MainActivity.this, \"Reset completed\", Toast.LENGTH_SHORT).show();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n";
        FileUtils.writeStringToFile(new File(javaDir, "MainActivity.java"), javaCode);

        String layoutXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:gravity=\"center\"\n" +
                "    android:orientation=\"vertical\"\n" +
                "    android:padding=\"24dp\"\n" +
                "    android:background=\"#F8F9FD\">\n\n" +
                "    <TextView\n" +
                "        android:id=\"@+id/tv_title\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"@string/welcome_msg\"\n" +
                "        android:textColor=\"#111827\"\n" +
                "        android:textSize=\"22sp\"\n" +
                "        android:textStyle=\"bold\" />\n\n" +
                "    <TextView\n" +
                "        android:id=\"@+id/tv_counter\"\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:text=\"Clicks: 0\"\n" +
                "        android:textColor=\"#534AEF\"\n" +
                "        android:textSize=\"36sp\"\n" +
                "        android:textStyle=\"bold\"\n" +
                "        android:layout_marginTop=\"28dp\"\n" +
                "        android:layout_marginBottom=\"28dp\" />\n\n" +
                "    <Button\n" +
                "        android:id=\"@+id/btn_increment\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"52dp\"\n" +
                "        android:text=\"Click to Count\"\n" +
                "        android:background=\"#534AEF\"\n" +
                "        android:textColor=\"#FFFFFF\"\n" +
                "        android:textSize=\"16sp\"\n" +
                "        android:textStyle=\"bold\" />\n\n" +
                "    <Button\n" +
                "        android:id=\"@+id/btn_reset\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"52dp\"\n" +
                "        android:text=\"Reset\"\n" +
                "        android:layout_marginTop=\"12dp\"\n" +
                "        android:background=\"#E5E7EB\"\n" +
                "        android:textColor=\"#374151\"\n" +
                "        android:textSize=\"15sp\" />\n\n" +
                "</LinearLayout>\n";
        FileUtils.writeStringToFile(new File(layoutDir, "main.xml"), layoutXml);
    }

    // ==========================================
    // 2. FAB APP TEMPLATE
    // ==========================================
    private static void generateFabAppTemplate(Project project, File javaDir, File layoutDir) throws IOException {
        String javaCode = "package " + project.getPackageName() + ";\n\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "import android.view.View;\n" +
                "import android.widget.ImageButton;\n" +
                "import android.widget.ListView;\n" +
                "import android.widget.ArrayAdapter;\n" +
                "import android.widget.Toast;\n" +
                "import java.util.ArrayList;\n\n" +
                "public class MainActivity extends Activity {\n\n" +
                "    private ImageButton fabAdd;\n" +
                "    private ListView listView;\n" +
                "    private ArrayList<String> itemsList;\n" +
                "    private ArrayAdapter<String> adapter;\n\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.main);\n\n" +
                "        fabAdd = findViewById(R.id.fab_add);\n" +
                "        listView = findViewById(R.id.list_view);\n\n" +
                "        itemsList = new ArrayList<String>();\n" +
                "        itemsList.add(\"Sample Task 1\");\n" +
                "        itemsList.add(\"Sample Task 2\");\n" +
                "        itemsList.add(\"Sample Task 3\");\n\n" +
                "        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsList);\n" +
                "        listView.setAdapter(adapter);\n\n" +
                "        fabAdd.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                int newIndex = itemsList.size() + 1;\n" +
                "                itemsList.add(\"New Added Item #\" + newIndex);\n" +
                "                adapter.notifyDataSetChanged();\n" +
                "                Toast.makeText(MainActivity.this, \"New item added!\", Toast.LENGTH_SHORT).show();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n" +
                "}\n";
        FileUtils.writeStringToFile(new File(javaDir, "MainActivity.java"), javaCode);

        String layoutXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:background=\"#F8F9FD\">\n\n" +
                "    <!-- Top Bar -->\n" +
                "    <LinearLayout\n" +
                "        android:id=\"@+id/top_bar\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"56dp\"\n" +
                "        android:background=\"#0288D1\"\n" +
                "        android:gravity=\"center_vertical\"\n" +
                "        android:paddingStart=\"16dp\"\n" +
                "        android:paddingEnd=\"16dp\">\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"@string/app_name\"\n" +
                "            android:textColor=\"#FFFFFF\"\n" +
                "            android:textSize=\"18sp\"\n" +
                "            android:textStyle=\"bold\" />\n" +
                "    </LinearLayout>\n\n" +
                "    <!-- Content List -->\n" +
                "    <ListView\n" +
                "        android:id=\"@+id/list_view\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"match_parent\"\n" +
                "        android:layout_below=\"@id/top_bar\"\n" +
                "        android:padding=\"8dp\" />\n\n" +
                "    <!-- Red/Pink Floating Action Button (FAB) -->\n" +
                "    <ImageButton\n" +
                "        android:id=\"@+id/fab_add\"\n" +
                "        android:layout_width=\"56dp\"\n" +
                "        android:layout_height=\"56dp\"\n" +
                "        android:layout_alignParentEnd=\"true\"\n" +
                "        android:layout_alignParentBottom=\"true\"\n" +
                "        android:layout_margin=\"24dp\"\n" +
                "        android:background=\"#E91E63\"\n" +
                "        android:src=\"@android:drawable/ic_input_add\" />\n\n" +
                "</RelativeLayout>\n";
        FileUtils.writeStringToFile(new File(layoutDir, "main.xml"), layoutXml);
    }

    // ==========================================
    // 3. NAVIGATION DRAWER TEMPLATE (Screenshot 6)
    // ==========================================
    private static void generateNavDrawerTemplate(Project project, File javaDir, File layoutDir) throws IOException {
        String javaCode = "package " + project.getPackageName() + ";\n\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "import android.view.View;\n" +
                "import android.widget.ImageButton;\n" +
                "import android.widget.LinearLayout;\n" +
                "import android.widget.TextView;\n" +
                "import android.widget.Toast;\n\n" +
                "public class MainActivity extends Activity {\n\n" +
                "    private ImageButton btnMenu;\n" +
                "    private LinearLayout drawerPane;\n" +
                "    private View drawerOverlay;\n" +
                "    private TextView menuItem1, menuItem2, menuItem3;\n\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        setContentView(R.layout.main);\n\n" +
                "        btnMenu = findViewById(R.id.btn_menu);\n" +
                "        drawerPane = findViewById(R.id.drawer_pane);\n" +
                "        drawerOverlay = findViewById(R.id.drawer_overlay);\n" +
                "        menuItem1 = findViewById(R.id.menu_item1);\n" +
                "        menuItem2 = findViewById(R.id.menu_item2);\n" +
                "        menuItem3 = findViewById(R.id.menu_item3);\n\n" +
                "        btnMenu.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                openDrawer();\n" +
                "            }\n" +
                "        });\n\n" +
                "        drawerOverlay.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                closeDrawer();\n" +
                "            }\n" +
                "        });\n\n" +
                "        menuItem1.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                Toast.makeText(MainActivity.this, \"Home Selected\", Toast.LENGTH_SHORT).show();\n" +
                "                closeDrawer();\n" +
                "            }\n" +
                "        });\n\n" +
                "        menuItem2.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                Toast.makeText(MainActivity.this, \"Settings Selected\", Toast.LENGTH_SHORT).show();\n" +
                "                closeDrawer();\n" +
                "            }\n" +
                "        });\n\n" +
                "        menuItem3.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                Toast.makeText(MainActivity.this, \"About Selected\", Toast.LENGTH_SHORT).show();\n" +
                "                closeDrawer();\n" +
                "            }\n" +
                "        });\n" +
                "    }\n\n" +
                "    private void openDrawer() {\n" +
                "        if (drawerPane != null) drawerPane.setVisibility(View.VISIBLE);\n" +
                "        if (drawerOverlay != null) drawerOverlay.setVisibility(View.VISIBLE);\n" +
                "    }\n\n" +
                "    private void closeDrawer() {\n" +
                "        if (drawerPane != null) drawerPane.setVisibility(View.GONE);\n" +
                "        if (drawerOverlay != null) drawerOverlay.setVisibility(View.GONE);\n" +
                "    }\n" +
                "}\n";
        FileUtils.writeStringToFile(new File(javaDir, "MainActivity.java"), javaCode);

        String layoutXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:background=\"#F8F9FD\">\n\n" +
                "    <!-- Top Toolbar -->\n" +
                "    <LinearLayout\n" +
                "        android:id=\"@+id/toolbar\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"56dp\"\n" +
                "        android:background=\"#37474F\"\n" +
                "        android:gravity=\"center_vertical\"\n" +
                "        android:paddingStart=\"12dp\"\n" +
                "        android:paddingEnd=\"12dp\">\n\n" +
                "        <ImageButton\n" +
                "            android:id=\"@+id/btn_menu\"\n" +
                "            android:layout_width=\"40dp\"\n" +
                "            android:layout_height=\"40dp\"\n" +
                "            android:background=\"?android:attr/selectableItemBackground\"\n" +
                "            android:src=\"@android:drawable/ic_menu_sort_by_size\" />\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"@string/app_name\"\n" +
                "            android:textColor=\"#FFFFFF\"\n" +
                "            android:textSize=\"18sp\"\n" +
                "            android:textStyle=\"bold\"\n" +
                "            android:layout_marginStart=\"12dp\" />\n" +
                "    </LinearLayout>\n\n" +
                "    <!-- Main Body Content -->\n" +
                "    <LinearLayout\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"match_parent\"\n" +
                "        android:layout_below=\"@id/toolbar\"\n" +
                "        android:gravity=\"center\"\n" +
                "        android:orientation=\"vertical\"\n" +
                "        android:padding=\"20dp\">\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Navigation Drawer Demo\"\n" +
                "            android:textColor=\"#111827\"\n" +
                "            android:textSize=\"20sp\"\n" +
                "            android:textStyle=\"bold\" />\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Tap the menu button at top-left to open the drawer\"\n" +
                "            android:textColor=\"#6B7280\"\n" +
                "            android:textSize=\"14sp\"\n" +
                "            android:layout_marginTop=\"8dp\" />\n" +
                "    </LinearLayout>\n\n" +
                "    <!-- Semi-transparent Drawer Overlay -->\n" +
                "    <View\n" +
                "        android:id=\"@+id/drawer_overlay\"\n" +
                "        android:layout_width=\"match_parent\"\n" +
                "        android:layout_height=\"match_parent\"\n" +
                "        android:background=\"#80000000\"\n" +
                "        android:visibility=\"gone\" />\n\n" +
                "    <!-- Slide-in Drawer Pane (Green Header) -->\n" +
                "    <LinearLayout\n" +
                "        android:id=\"@+id/drawer_pane\"\n" +
                "        android:layout_width=\"260dp\"\n" +
                "        android:layout_height=\"match_parent\"\n" +
                "        android:background=\"#FFFFFF\"\n" +
                "        android:orientation=\"vertical\"\n" +
                "        android:visibility=\"gone\">\n\n" +
                "        <!-- Green Drawer Header -->\n" +
                "        <LinearLayout\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"140dp\"\n" +
                "            android:background=\"#4CAF50\"\n" +
                "            android:gravity=\"bottom\"\n" +
                "            android:padding=\"16dp\"\n" +
                "            android:orientation=\"vertical\">\n\n" +
                "            <TextView\n" +
                "                android:layout_width=\"wrap_content\"\n" +
                "                android:layout_height=\"wrap_content\"\n" +
                "                android:text=\"User Account\"\n" +
                "                android:textColor=\"#FFFFFF\"\n" +
                "                android:textSize=\"18sp\"\n" +
                "                android:textStyle=\"bold\" />\n\n" +
                "            <TextView\n" +
                "                android:layout_width=\"wrap_content\"\n" +
                "                android:layout_height=\"wrap_content\"\n" +
                "                android:text=\"user@buildstudio.dev\"\n" +
                "                android:textColor=\"#E8F5E9\"\n" +
                "                android:textSize=\"13sp\" />\n" +
                "        </LinearLayout>\n\n" +
                "        <!-- Menu Items -->\n" +
                "        <TextView\n" +
                "            android:id=\"@+id/menu_item1\"\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:padding=\"18dp\"\n" +
                "            android:text=\"Home\"\n" +
                "            android:textColor=\"#111827\"\n" +
                "            android:textSize=\"15sp\" />\n\n" +
                "        <TextView\n" +
                "            android:id=\"@+id/menu_item2\"\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:padding=\"18dp\"\n" +
                "            android:text=\"Settings\"\n" +
                "            android:textColor=\"#111827\"\n" +
                "            android:textSize=\"15sp\" />\n\n" +
                "        <TextView\n" +
                "            android:id=\"@+id/menu_item3\"\n" +
                "            android:layout_width=\"match_parent\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:padding=\"18dp\"\n" +
                "            android:text=\"About\"\n" +
                "            android:textColor=\"#111827\"\n" +
                "            android:textSize=\"15sp\" />\n" +
                "    </LinearLayout>\n\n" +
                "</RelativeLayout>\n";
        FileUtils.writeStringToFile(new File(layoutDir, "main.xml"), layoutXml);
    }

    // ==========================================
    // 4. FULLSCREEN APP TEMPLATE
    // ==========================================
    private static void generateFullscreenAppTemplate(Project project, File javaDir, File layoutDir) throws IOException {
        String javaCode = "package " + project.getPackageName() + ";\n\n" +
                "import android.app.Activity;\n" +
                "import android.os.Bundle;\n" +
                "import android.view.View;\n" +
                "import android.view.Window;\n" +
                "import android.view.WindowManager;\n" +
                "import android.widget.Button;\n" +
                "import android.widget.Toast;\n\n" +
                "public class MainActivity extends Activity {\n\n" +
                "    private boolean isImmersive = true;\n" +
                "    private View decorView;\n" +
                "    private Button btnToggle;\n\n" +
                "    @Override\n" +
                "    protected void onCreate(Bundle savedInstanceState) {\n" +
                "        super.onCreate(savedInstanceState);\n" +
                "        requestWindowFeature(Window.FEATURE_NO_TITLE);\n" +
                "        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);\n" +
                "        setContentView(R.layout.main);\n\n" +
                "        decorView = getWindow().getDecorView();\n" +
                "        applyFullscreenFlags();\n\n" +
                "        btnToggle = findViewById(R.id.btn_toggle);\n" +
                "        btnToggle.setOnClickListener(new View.OnClickListener() {\n" +
                "            @Override\n" +
                "            public void onClick(View v) {\n" +
                "                isImmersive = !isImmersive;\n" +
                "                if (isImmersive) {\n" +
                "                    applyFullscreenFlags();\n" +
                "                    Toast.makeText(MainActivity.this, \"Full Immersion On\", Toast.LENGTH_SHORT).show();\n" +
                "                } else {\n" +
                "                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);\n" +
                "                    Toast.makeText(MainActivity.this, \"Standard View\", Toast.LENGTH_SHORT).show();\n" +
                "                }\n" +
                "            }\n" +
                "        });\n" +
                "    }\n\n" +
                "    private void applyFullscreenFlags() {\n" +
                "        decorView.setSystemUiVisibility(\n" +
                "            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |\n" +
                "            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |\n" +
                "            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |\n" +
                "            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |\n" +
                "            View.SYSTEM_UI_FLAG_FULLSCREEN |\n" +
                "            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY\n" +
                "        );\n" +
                "    }\n" +
                "}\n";
        FileUtils.writeStringToFile(new File(javaDir, "MainActivity.java"), javaCode);

        String layoutXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<RelativeLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    android:layout_width=\"match_parent\"\n" +
                "    android:layout_height=\"match_parent\"\n" +
                "    android:background=\"#0F172A\">\n\n" +
                "    <LinearLayout\n" +
                "        android:layout_width=\"wrap_content\"\n" +
                "        android:layout_height=\"wrap_content\"\n" +
                "        android:layout_centerInParent=\"true\"\n" +
                "        android:gravity=\"center\"\n" +
                "        android:orientation=\"vertical\">\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Fullscreen Immersive Canvas\"\n" +
                "            android:textColor=\"#38BDF8\"\n" +
                "            android:textSize=\"24sp\"\n" +
                "            android:textStyle=\"bold\" />\n\n" +
                "        <TextView\n" +
                "            android:layout_width=\"wrap_content\"\n" +
                "            android:layout_height=\"wrap_content\"\n" +
                "            android:text=\"Status Bar & Navigation Bar are Hidden\"\n" +
                "            android:textColor=\"#94A3B8\"\n" +
                "            android:textSize=\"14sp\"\n" +
                "            android:layout_marginTop=\"8dp\"\n" +
                "            android:layout_marginBottom=\"24dp\" />\n\n" +
                "        <Button\n" +
                "            android:id=\"@+id/btn_toggle\"\n" +
                "            android:layout_width=\"200dp\"\n" +
                "            android:layout_height=\"50dp\"\n" +
                "            android:text=\"Toggle System UI\"\n" +
                "            android:background=\"#38BDF8\"\n" +
                "            android:textColor=\"#0F172A\"\n" +
                "            android:textStyle=\"bold\" />\n" +
                "    </LinearLayout>\n\n" +
                "</RelativeLayout>\n";
        FileUtils.writeStringToFile(new File(layoutDir, "main.xml"), layoutXml);
    }
}
