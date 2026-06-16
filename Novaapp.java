import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.InetAddress;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

public class Novaapp extends Application {

    static final String APP_NAME    = "NOVA";
    static final String APP_VERSION = "3.1.0";
    static final String APP_TAGLINE = "Neural Optimized Virtual Assistant";

    static final BooleanProperty DARK_MODE = new SimpleBooleanProperty(false);
    static final DoubleProperty FONT_SIZE = new SimpleDoubleProperty(14.0);

    static String bg()           { return DARK_MODE.get() ? "#060010" : "#f0ecff"; }
    static String bg2()          { return DARK_MODE.get() ? "#0a0018" : "#e8e2ff"; }
    static String glass()        { return DARK_MODE.get() ? "#ffffff0d" : "#00000008"; }
    static String glassBorder()  { return DARK_MODE.get() ? "#ffffff22" : "#00000018"; }
    static String panelBg()      { return DARK_MODE.get() ? "#ffffff06" : "#00000006"; }
    static String panelBorder()  { return DARK_MODE.get() ? "#ffffff12" : "#00000012"; }
    static String textMain()     { return DARK_MODE.get() ? "#e8e0ff" : "#1a0050"; }
    static String textDim()      { return DARK_MODE.get() ? "#aa99cc" : "#554488"; }
    static String purple()       { return "#9b30ff"; }
    static String cyan()         { return DARK_MODE.get() ? "#00f5ff" : "#0099aa"; }
    static String magenta()      { return "#ff2d78"; }
    static String inputBg()      { return DARK_MODE.get() ? "#ffffff0d" : "#00000008"; }
    static String topBarBg()     { return DARK_MODE.get() ? "#ffffff08" : "#00000008"; }

    static String fontMain()  { return "-fx-font-family:'Segoe UI',Arial; -fx-font-size:" + (int)FONT_SIZE.get() + "px;"; }
    static String fontDim()   { return "-fx-font-family:'Segoe UI',Arial; -fx-font-size:" + (int)(FONT_SIZE.get()-2) + "px;"; }
    static String fontMono()  { return "-fx-font-family:'Courier New'; -fx-font-size:" + (int)(FONT_SIZE.get()-2) + "px;"; }
    static String fontLabel() { return "-fx-font-family:'Courier New'; -fx-font-size:" + (int)(FONT_SIZE.get()-3) + "px; -fx-letter-spacing:2px;"; }

    static final String FONT_MAIN  = "-fx-font-family:'Segoe UI',Arial; -fx-font-size:14px;";
    static final String FONT_DIM   = "-fx-font-family:'Segoe UI',Arial; -fx-font-size:12px;";
    static final String FONT_MONO  = "-fx-font-family:'Courier New'; -fx-font-size:12px;";
    static final String FONT_LABEL = "-fx-font-family:'Courier New'; -fx-font-size:11px; -fx-letter-spacing:2px;";

   // network monitor
    // Shared state updated by a background thread
    static final AtomicLong  NET_PING_MS      = new AtomicLong(-1);   // -1 = not yet measured
    static final AtomicBoolean NET_REACHABLE  = new AtomicBoolean(false);
    static final StringProperty NET_LABEL     = new SimpleStringProperty("CHECKING…");
    static final StringProperty NET_COLOR     = new SimpleStringProperty("#ffcc44");

    static {

        ScheduledExecutorService net = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nova-net-ping");
            t.setDaemon(true);
            return t;
        });
        net.scheduleAtFixedRate(() -> {
            try {
                long start = System.currentTimeMillis();
                boolean ok = InetAddress.getByName("8.8.8.8").isReachable(2000);
                long ms = System.currentTimeMillis() - start;
                NET_REACHABLE.set(ok);
                NET_PING_MS.set(ok ? ms : -1);
                String label, color;
                if (!ok) {
                    label = "OFFLINE"; color = "#ff2d78";
                } else if (ms < 80) {
                    label = ms + "ms ●"; color = "#28c840";
                } else if (ms < 200) {
                    label = ms + "ms ●"; color = "#ffcc44";
                } else {
                    label = ms + "ms ●"; color = "#ff2d78";
                }
                final String fl = label, fc = color;
                Platform.runLater(() -> { NET_LABEL.set(fl); NET_COLOR.set(fc); });
            } catch (Exception e) {
                Platform.runLater(() -> { NET_LABEL.set("OFFLINE"); NET_COLOR.set("#ff2d78"); });
            }
        }, 0, 4, TimeUnit.SECONDS);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.UNDECORATED);
        new SplashScreen(primaryStage).show();
    }

    public static void main(String[] args) { launch(args); }


    //  WEB OPENER

    static class WebOpener {

        private static final Map<String, String> SITE_MAP = new LinkedHashMap<>();

        static {
            SITE_MAP.put("youtube",    "https://www.youtube.com");
            SITE_MAP.put("google",     "https://www.google.com");
            SITE_MAP.put("gmail",      "https://mail.google.com");
            SITE_MAP.put("facebook",   "https://www.facebook.com");
            SITE_MAP.put("instagram",  "https://www.instagram.com");
            SITE_MAP.put("twitter",    "https://www.twitter.com");
            SITE_MAP.put("x",          "https://www.x.com");
            SITE_MAP.put("github",     "https://www.github.com");
            SITE_MAP.put("stackoverflow", "https://stackoverflow.com");
            SITE_MAP.put("wikipedia",  "https://www.wikipedia.org");
            SITE_MAP.put("reddit",     "https://www.reddit.com");
            SITE_MAP.put("netflix",    "https://www.netflix.com");
            SITE_MAP.put("spotify",    "https://www.spotify.com");
            SITE_MAP.put("amazon",     "https://www.amazon.com");
            SITE_MAP.put("whatsapp",   "https://web.whatsapp.com");
            SITE_MAP.put("linkedin",   "https://www.linkedin.com");
            SITE_MAP.put("maps",       "https://maps.google.com");
            SITE_MAP.put("translate",  "https://translate.google.com");
            SITE_MAP.put("drive",      "https://drive.google.com");
            SITE_MAP.put("docs",       "https://docs.google.com");
        }

        static String handle(String input) {
            String lower = input.toLowerCase();
            if (!hasOpenIntent(lower)) return null;
            String site = detectSite(lower);
            if (site != null) {
                String url = SITE_MAP.get(site);
                openUrl(url);
                return "Opening " + capitalize(site) + " in your browser now! 🌐";
            }
            String rawUrl = extractUrl(lower);
            if (rawUrl != null) {
                openUrl(rawUrl);
                return "Opening " + rawUrl + " in your browser! 🌐";
            }
            String query = stripOpenIntent(lower);
            if (!query.isEmpty()) {
                String searchUrl = "https://www.google.com/search?q=" + query.replace(" ", "+");
                openUrl(searchUrl);
                return "Searching Google for \"" + query + "\"... 🔍";
            }
            return "Which website would you like to open? Try: open youtube, open github, etc.";
        }

        static boolean hasOpenIntent(String input) {
            String[] keywords = {
                    "open", "launch", "go to", "goto", "navigate to",
                    "take me to", "show me", "can you open", "can u open",
                    "please open", "start", "visit", "browse"
            };
            for (String k : keywords) if (input.contains(k)) return true;
            return false;
        }

        private static String detectSite(String input) {
            for (String site : SITE_MAP.keySet()) {
                if (input.contains(site)) return site;
            }
            return null;
        }

        private static String extractUrl(String input) {
            String[] words = input.split("\\s+");
            for (String w : words) {
                if (w.startsWith("http://") || w.startsWith("https://") || w.contains(".com") || w.contains(".org")) {
                    if (!w.startsWith("http")) w = "https://" + w;
                    return w;
                }
            }
            return null;
        }

        private static String stripOpenIntent(String input) {
            String[] prefixes = {
                    "can you open", "can u open", "please open", "take me to",
                    "navigate to", "go to", "goto", "show me", "browse", "visit", "open", "launch", "start"
            };
            for (String p : prefixes) {
                if (input.startsWith(p)) return input.substring(p.length()).trim();
            }
            return input.trim();
        }

        static void openUrl(String url) {
            try {
                if (!url.startsWith("http")) url = "https://" + url;
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win"))       Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    else if (os.contains("mac"))  Runtime.getRuntime().exec("open " + url);
                    else                          Runtime.getRuntime().exec("xdg-open " + url);
                }
            } catch (Exception e) {
                System.out.println("WebOpener error: " + e.getMessage());
            }
        }

        private static String capitalize(String s) {
            return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        static Map<String, String> getSiteMap() { return SITE_MAP; }
    }

    //App Launcher
    static class AppLauncher {

        record AppEntry(String icon, String name, String desc, String color, Runnable action) {}

        static List<AppEntry> getApps() {
            String os = System.getProperty("os.name").toLowerCase();
            List<AppEntry> apps = new ArrayList<>();
            if (os.contains("win")) {
                apps.add(new AppEntry("📝", "Notepad",       "Text editor",         "#00c8d4", () -> exec("notepad")));
                apps.add(new AppEntry("🧮", "Calculator",    "Math calculator",     "#9b30ff", () -> exec("calc")));
                apps.add(new AppEntry("📁", "File Explorer", "Browse files",        "#ffcc44", () -> exec("explorer")));
                apps.add(new AppEntry("⚙", "Task Manager",  "System processes",    "#ff2d78", () -> exec("taskmgr")));
                apps.add(new AppEntry("🔧", "Settings",      "Windows settings",    "#ffcc44", () -> exec("ms-settings:")));
                apps.add(new AppEntry("🌐", "Browser",       "Default web browser", "#ff2d78", () -> WebOpener.openUrl("https://www.google.com")));
                apps.add(new AppEntry("🎵", "Media Player",  "Play music/video",    "#28c840", () -> exec("wmplayer")));
            } else if (os.contains("mac")) {
                apps.add(new AppEntry("📝", "TextEdit",      "Text editor",         "#00c8d4", () -> execArr("open", "-a", "TextEdit")));
                apps.add(new AppEntry("🧮", "Calculator",    "Math calculator",     "#9b30ff", () -> execArr("open", "-a", "Calculator")));
                apps.add(new AppEntry("📁", "Finder",        "Browse files",        "#ffcc44", () -> execArr("open", "~")));
                apps.add(new AppEntry("💻", "Terminal",      "Command line",        "#9b30ff", () -> execArr("open", "-a", "Terminal")));
                apps.add(new AppEntry("🌐", "Safari",        "Web browser",         "#ff2d78", () -> execArr("open", "-a", "Safari")));
                apps.add(new AppEntry("⚙", "System Prefs",  "Mac settings",        "#28c840", () -> execArr("open", "-a", "System Preferences")));
            } else {
                apps.add(new AppEntry("📝", "Text Editor",   "Gedit / Kate",        "#00c8d4", () -> execArr("gedit")));
                apps.add(new AppEntry("🧮", "Calculator",    "gnome-calculator",    "#9b30ff", () -> execArr("gnome-calculator")));
                apps.add(new AppEntry("📁", "Files",         "Nautilus file manager","#ffcc44",() -> execArr("nautilus")));
                apps.add(new AppEntry("💻", "Terminal",      "System terminal",     "#9b30ff", () -> execArr("gnome-terminal")));
                apps.add(new AppEntry("🌐", "Browser",       "Default browser",     "#ff2d78", () -> execArr("xdg-open", "https://www.google.com")));
            }
            return apps;
        }

        static String handle(String input) {
            String lower = input.toLowerCase();
            if (!lower.contains("open") && !lower.contains("launch") && !lower.contains("start")) return null;
            for (AppEntry app : getApps()) {
                if (lower.contains(app.name().toLowerCase())) {
                    app.action().run();
                    return "Launching " + app.name() + "! 🚀";
                }
            }
            return null;
        }

        private static void exec(String cmd) {
            try { Runtime.getRuntime().exec(cmd); }
            catch (Exception e) { System.out.println("AppLauncher exec error: " + e.getMessage()); }
        }

        private static void execArr(String... cmd) {
            try { Runtime.getRuntime().exec(cmd); }
            catch (Exception e) { System.out.println("AppLauncher execArr error: " + e.getMessage()); }
        }
    }

    // Splash screen
    static class SplashScreen {

        private final Stage     stage;
        private       StackPane root;

        static class Particle {
            double x, y, vx, vy, radius, alpha, alphaDir;
            int    colorType;

            Particle(Random rng, double w, double h) {
                x = rng.nextDouble() * w;
                y = rng.nextDouble() * h;
                radius = 1.5 + rng.nextDouble() * 7.0;
                double speed = 0.15 + rng.nextDouble() * 0.55;
                double angle = rng.nextDouble() * Math.PI * 2;
                vx = Math.cos(angle) * speed;
                vy = Math.sin(angle) * speed;
                alpha = 0.2 + rng.nextDouble() * 0.75;
                alphaDir = (rng.nextBoolean() ? 1 : -1) * (0.003 + rng.nextDouble() * 0.007);
                colorType = rng.nextInt(4);
            }

            void update(double w, double h) {
                x += vx; y += vy;
                if (x < 0 || x > w) { vx = -vx; x = Math.max(0, Math.min(w, x)); }
                if (y < 0 || y > h) { vy = -vy; y = Math.max(0, Math.min(h, y)); }
                alpha += alphaDir;
                if (alpha > 0.95 || alpha < 0.08) alphaDir = -alphaDir;
            }

            Color color() {
                return switch (colorType) {
                    case 0 -> Color.color(0.608, 0.188, 1.0,   alpha);
                    case 1 -> Color.color(0.0,   0.961, 1.0,   alpha);
                    case 2 -> Color.color(1.0,   0.176, 0.471, alpha);
                    default-> Color.color(0.85,  0.80,  1.0,   alpha * 0.6);
                };
            }

            static void drawLine(GraphicsContext gc, Particle a, Particle b) {
                double dx = a.x - b.x, dy = a.y - b.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 90) {
                    gc.setStroke(Color.color(0.608, 0.188, 1.0, (1 - dist / 90) * 0.15));
                    gc.setLineWidth(0.5);
                    gc.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
        }

        SplashScreen(Stage stage) { this.stage = stage; }

        void show() {
            final double W = 900, H = 600;
            root = new StackPane();
            root.setPrefSize(W, H);
            root.setStyle("-fx-background-color: #000008;");

            Canvas cosmicCanvas = new Canvas(W, H);
            GraphicsContext cgc = cosmicCanvas.getGraphicsContext2D();
            Random rng = new Random(42);

            double[] starX      = new double[280];
            double[] starY      = new double[280];
            double[] starSize   = new double[280];
            double[] starAlpha  = new double[280];
            double[] starTwinkleOffset = new double[280];
            for (int i = 0; i < 280; i++) {
                starX[i]             = rng.nextDouble() * W;
                starY[i]             = rng.nextDouble() * H;
                starSize[i]          = 0.4 + rng.nextDouble() * 2.2;
                starAlpha[i]         = 0.3 + rng.nextDouble() * 0.7;
                starTwinkleOffset[i] = rng.nextDouble() * Math.PI * 2;
            }

            double[] ringAngle    = {0};
            double[] innerAngle   = {0};
            double[] coreBreath   = {1.0};

            Canvas uiCanvas = new Canvas(W, H);
            GraphicsContext ugc = uiCanvas.getGraphicsContext2D();
            Pane corners = buildCornerAccents(W, H);

            Label vBadge = new Label("v" + APP_VERSION);
            vBadge.setStyle("-fx-text-fill: #00f5ff88; -fx-font-size: 12px; -fx-font-family: 'Courier New';");
            StackPane.setAlignment(vBadge, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(vBadge, new Insets(0, 18, 14, 0));

            VBox center = buildCenterContent();
            Canvas scanlines = buildScanlineOverlay(W, H);
            root.getChildren().addAll(cosmicCanvas, uiCanvas, center, corners, vBadge, scanlines);

            Scene scene = new Scene(root, W, H);
            scene.setFill(Color.web("#000008"));
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            AnimationTimer timer = new AnimationTimer() {
                @Override public void handle(long now) {
                    double t = now * 1e-9;
                    ringAngle[0]  = t * 0.18;
                    innerAngle[0] = -t * 0.28;
                    coreBreath[0] = 0.92 + 0.08 * Math.sin(t * 1.8);

                    cgc.setFill(Color.color(0, 0, 0.03, 1.0));
                    cgc.fillRect(0, 0, W, H);

                    drawNebulaBlob(cgc, W*0.5, H*0.5, 520, 0.38, 0.05, 0.75, 0.06 + 0.015*Math.sin(t*0.4));
                    drawNebulaBlob(cgc, W*0.38 + Math.sin(t*0.22)*30, H*0.45 + Math.cos(t*0.18)*20, 300, 0.0, 0.6, 1.0, 0.055 + 0.01*Math.sin(t*0.5));
                    drawNebulaBlob(cgc, W*0.62 + Math.sin(t*0.19)*25, H*0.55 + Math.cos(t*0.23)*18, 260, 0.8, 0.1, 1.0, 0.048 + 0.01*Math.cos(t*0.6));
                    drawNebulaBlob(cgc, W*0.5, H*0.5, 180, 1.0, 0.55, 0.15, 0.04 + 0.01*Math.sin(t*0.7));
                    drawNebulaBlob(cgc, W*0.5, H*0.5, 90, 1.0, 0.9, 1.0, 0.18 + 0.04*Math.sin(t*2.0));

                    for (int i = 0; i < 280; i++) {
                        double twinkle = 0.4 + 0.6 * (0.5 + 0.5*Math.sin(t * (1.2 + 0.8*(i%7)) + starTwinkleOffset[i]));
                        double a = starAlpha[i] * twinkle;
                        cgc.setGlobalAlpha(a);
                        if (i % 5 == 0) cgc.setFill(Color.color(0.6, 0.85, 1.0, 1));
                        else if (i % 5 == 1) cgc.setFill(Color.color(1, 0.85, 0.6, 1));
                        else cgc.setFill(Color.WHITE);
                        double sz = starSize[i] * twinkle;
                        if (starSize[i] > 1.8 && twinkle > 0.85) {
                            cgc.setStroke(cgc.getFill());
                            cgc.setLineWidth(0.5);
                            cgc.setGlobalAlpha(a * 0.5);
                            cgc.strokeLine(starX[i]-sz*2, starY[i], starX[i]+sz*2, starY[i]);
                            cgc.strokeLine(starX[i], starY[i]-sz*2, starX[i], starY[i]+sz*2);
                        }
                        cgc.setGlobalAlpha(a);
                        cgc.fillOval(starX[i]-sz/2, starY[i]-sz/2, sz, sz);
                    }
                    cgc.setGlobalAlpha(1.0);

                    ugc.clearRect(0, 0, W, H);
                    double cx = W / 2.0, cy = H / 2.0;

                    int segments = 18;
                    double outerR = 210;
                    for (int i = 0; i < segments; i++) {
                        double angle    = ringAngle[0] + (i / (double)segments) * Math.PI * 2;
                        double segLen   = (i % 3 == 0) ? 0.28 : (i % 3 == 1) ? 0.14 : 0.06;
                        double gapFrac  = 0.03;
                        double startDeg = Math.toDegrees(angle) + gapFrac * 360 / segments;
                        double extentDeg= segLen * 360;
                        double alpha    = 0.5 + 0.5 * Math.sin(t * 0.9 + i * 0.7);
                        double thickness= (i % 3 == 0) ? 3.5 : 2.0;
                        Color segColor;
                        if (i % 3 == 0) segColor = Color.color(0.0, 0.96, 1.0, alpha * 0.85);
                        else if (i % 3 == 1) segColor = Color.color(0.61, 0.19, 1.0, alpha * 0.75);
                        else segColor = Color.color(1.0, 0.18, 0.47, alpha * 0.55);
                        ugc.setStroke(segColor);
                        ugc.setLineWidth(thickness);
                        ugc.setLineCap(StrokeLineCap.ROUND);
                        ugc.strokeArc(cx - outerR, cy - outerR, outerR*2, outerR*2, startDeg, extentDeg, ArcType.OPEN);
                        ugc.setStroke(Color.color(segColor.getRed(), segColor.getGreen(), segColor.getBlue(), alpha * 0.2));
                        ugc.setLineWidth(thickness + 6);
                        ugc.strokeArc(cx - outerR, cy - outerR, outerR*2, outerR*2, startDeg, extentDeg, ArcType.OPEN);
                    }

                    double midR = 152;
                    int midSegs = 8;
                    for (int i = 0; i < midSegs; i++) {
                        double angle    = innerAngle[0] + (i / (double)midSegs) * Math.PI * 2;
                        double startDeg = Math.toDegrees(angle);
                        double extentDeg= (i % 2 == 0) ? 24 : 10;
                        double alpha    = 0.45 + 0.35 * Math.sin(t * 1.4 + i);
                        ugc.setStroke(Color.color(0.0, 0.96, 1.0, alpha));
                        ugc.setLineWidth(2.0);
                        ugc.strokeArc(cx - midR, cy - midR, midR*2, midR*2, startDeg, extentDeg, ArcType.OPEN);
                    }

                    double innerR = 100 * coreBreath[0];
                    ugc.setStroke(Color.color(0.61, 0.19, 1.0, 0.55 + 0.2*Math.sin(t*2.2)));
                    ugc.setLineWidth(1.5);
                    ugc.strokeOval(cx - innerR, cy - innerR, innerR*2, innerR*2);

                    int nodeCount = 12;
                    for (int i = 0; i < nodeCount; i++) {
                        double ang = ringAngle[0] + (i / (double)nodeCount) * Math.PI * 2;
                        double nx = cx + outerR * Math.cos(ang);
                        double ny = cy + outerR * Math.sin(ang);
                        double da = 0.5 + 0.5*Math.sin(t * 2.5 + i * 0.9);
                        boolean isBig = (i % 4 == 0);
                        double nr = isBig ? 5 : 2.5;
                        ugc.setFill(Color.color(0.0, 0.96, 1.0, da * 0.25));
                        ugc.fillOval(nx - nr*3, ny - nr*3, nr*6, nr*6);
                        ugc.setFill(isBig ? Color.color(1, 0.95, 0.7, da) : Color.color(0, 0.96, 1.0, da));
                        ugc.fillOval(nx - nr, ny - nr, nr*2, nr*2);
                    }

                    int spokeCount = 6;
                    for (int i = 0; i < spokeCount; i++) {
                        double ang = ringAngle[0] * 1.4 + (i / (double)spokeCount) * Math.PI * 2;
                        double sx1 = cx + innerR * Math.cos(ang);
                        double sy1 = cy + innerR * Math.sin(ang);
                        double sx2 = cx + (outerR - 12) * Math.cos(ang);
                        double sy2 = cy + (outerR - 12) * Math.sin(ang);
                        double sa  = 0.08 + 0.07 * Math.sin(t * 1.6 + i);
                        ugc.setStroke(Color.color(0.61, 0.19, 1.0, sa));
                        ugc.setLineWidth(1.0);
                        ugc.strokeLine(sx1, sy1, sx2, sy2);
                    }

                    double coreR = 28 * coreBreath[0];
                    drawNebulaBlob(ugc, cx, cy, coreR * 4, 1.0, 0.97, 1.0, 0.35 * coreBreath[0]);
                    drawNebulaBlob(ugc, cx, cy, coreR * 2, 1.0, 1.0, 1.0, 0.6);
                    ugc.setFill(Color.color(1, 1, 1, 0.95));
                    ugc.fillOval(cx - coreR*0.25, cy - coreR*0.25, coreR*0.5, coreR*0.5);

                    for (int p = 0; p < 4; p++) {
                        double pa = t * 0.12 + p * Math.PI / 2;
                        double fLen = coreR * (2.2 + 0.4*Math.sin(t*1.7 + p));
                        double fAlpha = 0.25 + 0.12*Math.sin(t*2.0 + p);
                        ugc.setStroke(Color.color(0.85, 0.95, 1.0, fAlpha));
                        ugc.setLineWidth(1.5);
                        ugc.strokeLine(cx, cy, cx + fLen*Math.cos(pa), cy + fLen*Math.sin(pa));
                    }
                }

                void drawNebulaBlob(GraphicsContext gc, double x, double y, double r,
                                    double red, double green, double blue, double alpha) {
                    gc.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                            new Stop(0, Color.color(red, green, blue, alpha)),
                            new Stop(0.45, Color.color(red, green, blue, alpha * 0.35)),
                            new Stop(1, Color.color(red, green, blue, 0))));
                    gc.fillOval(x - r/2, y - r/2, r, r);
                }
            };
            timer.start();

            PauseTransition delay = new PauseTransition(Duration.seconds(5.0));
            delay.setOnFinished(e -> {
                timer.stop();
                FadeTransition ft = new FadeTransition(Duration.millis(600), root);
                ft.setToValue(0);
                ft.setOnFinished(ev -> {
                    stage.setWidth(1260); stage.setHeight(780);
                    stage.centerOnScreen();
                    new MainWindow(stage).show();
                });
                ft.play();
            });
            delay.play();
        }

        private Canvas buildScanlineOverlay(double W, double H) {
            Canvas c = new Canvas(W, H);
            GraphicsContext gc = c.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            for (int y = 0; y < H; y += 4) { gc.setGlobalAlpha(0.04); gc.fillRect(0, y, W, 1); }
            c.setMouseTransparent(true);
            return c;
        }

        private Pane buildCornerAccents(double W, double H) {
            Pane p = new Pane(); p.setPrefSize(W, H); p.setMouseTransparent(true);
            addL(p, 0, 0,  30, 1.5, true,  true);
            addL(p, W, 0, -30, 1.5, false, true);
            addL(p, 0, H,  30, 1.5, true,  false);
            addL(p, W, H, -30, 1.5, false, false);
            return p;
        }

        private void addL(Pane p, double cx, double cy, double len, double thick, boolean right, boolean down) {
            double l = Math.abs(len), t = thick;
            Rectangle h = new Rectangle(right ? cx : cx - l, down ? cy : cy - t, l, t);
            h.setFill(Color.web("#00f5ff"));
            Rectangle v = new Rectangle(right ? cx : cx - t, down ? cy : cy - l, t, l);
            v.setFill(Color.web("#00f5ff"));
            p.getChildren().addAll(h, v);
        }

        private VBox buildCenterContent() {
            VBox box = new VBox(18); box.setAlignment(Pos.CENTER);
            StackPane orb = buildStaticOrb();

            Label nameLabel = new Label(APP_NAME);
            nameLabel.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 92px; -fx-font-weight: bold; -fx-text-fill: white;");
            DropShadow outerGlow = new DropShadow(80, Color.web("#9b30ffcc"));
            outerGlow.setSpread(0.15);
            DropShadow innerGlow = new DropShadow(35, Color.web("#00f5ffee"));
            innerGlow.setSpread(0.3);
            innerGlow.setInput(new Bloom(0.05));
            outerGlow.setInput(innerGlow);
            nameLabel.setEffect(outerGlow);

            Timeline glowPulse = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(outerGlow.radiusProperty(), 80),
                            new KeyValue(innerGlow.radiusProperty(), 35)),
                    new KeyFrame(Duration.seconds(1.4),
                            new KeyValue(outerGlow.radiusProperty(), 110),
                            new KeyValue(innerGlow.radiusProperty(), 55))
            );
            glowPulse.setCycleCount(Animation.INDEFINITE);
            glowPulse.setAutoReverse(true);
            glowPulse.play();

            Label tagLabel = new Label(APP_TAGLINE.toUpperCase());
            tagLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #9b30ff; -fx-font-weight: bold;");
            tagLabel.setEffect(new DropShadow(12, Color.web("#9b30ff")));

            StackPane loadBar = buildLoadingBar();

            Label status = new Label("INITIALIZING NEURAL CORE...");
            status.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-text-fill: #00f5ffaa;");
            animateStatus(status);

            box.getChildren().addAll(orb, nameLabel, tagLabel, loadBar, status);
            box.setOpacity(0); box.setTranslateY(28);
            FadeTransition ft = new FadeTransition(Duration.millis(900), box);
            ft.setToValue(1); ft.setDelay(Duration.millis(250));
            TranslateTransition tt = new TranslateTransition(Duration.millis(900), box);
            tt.setToY(0); tt.setDelay(Duration.millis(250)); tt.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, tt).play();
            return box;
        }

        private StackPane buildStaticOrb() {
            Circle outer = new Circle(54); outer.setFill(Color.TRANSPARENT);
            outer.setStroke(Color.web("#9b30ff55")); outer.setStrokeWidth(1.5);
            outer.setEffect(new DropShadow(18, Color.web("#9b30ff")));

            Circle mid = new Circle(44, Color.web("#9b30ff11"));
            mid.setStroke(Color.web("#9b30ff44")); mid.setStrokeWidth(1);

            Circle inner = new Circle(30, Color.web("#9b30ff22"));
            inner.setStroke(Color.web("#9b30ff88")); inner.setStrokeWidth(1.5);
            inner.setEffect(new DropShadow(28, Color.web("#9b30ff")));
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.8), inner);
            pulse.setFromX(0.90); pulse.setFromY(0.90); pulse.setToX(1.10); pulse.setToY(1.10);
            pulse.setCycleCount(Animation.INDEFINITE); pulse.setAutoReverse(true); pulse.play();

            Circle core = new Circle(10, Color.web("#ffffff"));
            core.setEffect(new DropShadow(20, Color.web("#00f5ff")));
            ScaleTransition corePulse = new ScaleTransition(Duration.seconds(1.4), core);
            corePulse.setFromX(0.85); corePulse.setFromY(0.85); corePulse.setToX(1.15); corePulse.setToY(1.15);
            corePulse.setCycleCount(Animation.INDEFINITE); corePulse.setAutoReverse(true); corePulse.play();

            Label icon = new Label("◈");
            icon.setStyle("-fx-font-size: 26px; -fx-text-fill: #00f5ff;");
            icon.setEffect(new DropShadow(16, Color.web("#00f5ff")));

            ScaleTransition outerPulse = new ScaleTransition(Duration.seconds(2.2), outer);
            outerPulse.setFromX(0.95); outerPulse.setFromY(0.95); outerPulse.setToX(1.05); outerPulse.setToY(1.05);
            outerPulse.setCycleCount(Animation.INDEFINITE); outerPulse.setAutoReverse(true); outerPulse.play();

            return new StackPane(outer, mid, inner, icon);
        }

        private StackPane buildLoadingBar() {
            Rectangle bg   = new Rectangle(340, 3); bg.setFill(Color.web("#ffffff10")); bg.setArcWidth(3); bg.setArcHeight(3);
            Rectangle fill = new Rectangle(0, 3);
            fill.setFill(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE, new Stop(0,Color.web("#9b30ff")), new Stop(1,Color.web("#00f5ff"))));
            fill.setArcWidth(3); fill.setArcHeight(3); fill.setEffect(new DropShadow(8, Color.web("#00f5ff")));
            new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(fill.widthProperty(), 0)),
                    new KeyFrame(Duration.seconds(3.5), new KeyValue(fill.widthProperty(), 340, Interpolator.EASE_BOTH))).play();
            StackPane bar = new StackPane(bg, fill); bar.setAlignment(Pos.CENTER_LEFT);
            return bar;
        }

        private void animateStatus(Label lbl) {
            String[] msgs = {
                    "INITIALIZING NEURAL CORE...", "LOADING LANGUAGE MODELS...",
                    "CALIBRATING VOICE ENGINE...", "SCANNING SYSTEM RESOURCES...",
                    "LOADING WEB & APP MODULES...", "NOVA IS READY."
            };
            int[] idx = {0};
            Timeline tl = new Timeline(new KeyFrame(Duration.millis(620), e -> {
                if (idx[0] < msgs.length) lbl.setText(msgs[idx[0]++]);
            }));
            tl.setCycleCount(msgs.length); tl.play();
        }
    }

    //Main window
    static class MainWindow {

        private final Stage   stage;
        private       double  dragX, dragY;
        private final StringProperty activeSection = new SimpleStringProperty("Dashboard");
        private       VBox    centerContent;
        private       BorderPane rootPane;
        private       StackPane  fullRoot;

        MainWindow(Stage stage) { this.stage = stage; }

        void show() { buildAndDisplay(); }

        private void buildAndDisplay() {
            rootPane = new BorderPane();
            rootPane.setStyle("-fx-background-color: " + bg() + ";");
            rootPane.setPrefSize(1260, 780);

            StackPane bg      = buildBackground();
            HBox  topBar      = buildTopBar();
            VBox  sidebar     = buildSidebar();
            VBox  chat        = buildChatArea();
            VBox  right       = buildRightPanel();
            HBox  dock        = buildDock();

            centerContent = chat;
            rootPane.setTop(topBar); rootPane.setLeft(sidebar);
            rootPane.setCenter(chat); rootPane.setRight(right); rootPane.setBottom(dock);

            fullRoot = new StackPane(bg, rootPane);
            Scene scene = new Scene(fullRoot, 1260, 780);
            scene.setFill(Color.web(bg()));

            topBar.setOnMousePressed(e -> { dragX = e.getSceneX(); dragY = e.getSceneY(); });
            topBar.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - dragX); stage.setY(e.getScreenY() - dragY); });

            scene.setOnKeyPressed(e -> handleGlobalKeyShortcuts(e, scene));

            stage.setScene(scene);
            stage.show();
            DARK_MODE.addListener((obs, oldVal, newVal) -> rebuildTheme());
            FONT_SIZE.addListener((obs, oldVal, newVal) -> rebuildTheme());
        }

        private void handleGlobalKeyShortcuts(KeyEvent e, Scene scene) {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case N -> { activeSection.set("Chat");      switchSection("Chat"); }
                    case D -> { activeSection.set("Analytics"); switchSection("Analytics"); }
                    case T -> DARK_MODE.set(!DARK_MODE.get());
                    case L -> { activeSection.set("Chat");      switchSection("Chat"); }
                    case COMMA -> { activeSection.set("Settings");  switchSection("Settings"); }
                    case P -> showCommandPalette();
                    case H -> { activeSection.set("Dashboard"); switchSection("Dashboard"); }
                    case E -> showToast("Chat exported! (demo)");
                    default -> {}
                }
            }
        }

        private void rebuildTheme() {
            rootPane.setStyle("-fx-background-color: " + bg() + ";");
            stage.getScene().setFill(Color.web(bg()));
            HBox  newTop     = buildTopBar();
            VBox  newSidebar = buildSidebar();
            VBox  newRight   = buildRightPanel();
            HBox  newDock    = buildDock();
            VBox  newChat    = buildChatSection(activeSection.get());
            newTop.setOnMousePressed(e -> { dragX = e.getSceneX(); dragY = e.getSceneY(); });
            newTop.setOnMouseDragged(e -> { stage.setX(e.getScreenX() - dragX); stage.setY(e.getScreenY() - dragY); });
            rootPane.setTop(newTop); rootPane.setLeft(newSidebar);
            rootPane.setCenter(newChat); rootPane.setRight(newRight); rootPane.setBottom(newDock);
            centerContent = newChat;
            fullRoot.getChildren().set(0, buildBackground());
            stage.getScene().setOnKeyPressed(ev -> handleGlobalKeyShortcuts(ev, stage.getScene()));
        }

        private StackPane buildBackground() {
            Canvas c = new Canvas(1260, 780);
            GraphicsContext gc = c.getGraphicsContext2D();
            double bgAlpha = DARK_MODE.get() ? 0.07 : 0.04;
            new AnimationTimer() {
                double t = 0;
                @Override public void handle(long now) {
                    t += 0.01;
                    gc.clearRect(0,0,1260,780);
                    gc.setStroke(Color.web(DARK_MODE.get() ? "#9b30ff10" : "#9b30ff18")); gc.setLineWidth(0.5);
                    for (int x=0; x<=1260; x+=48) gc.strokeLine(x,0,x,780);
                    for (int y=0; y<=780;  y+=48) gc.strokeLine(0,y,1260,y);
                    drawBlob(gc, 640+Math.sin(t*.3)*190, 400+Math.cos(t*.2)*110, 380, 0.608,0.0,1.0,   bgAlpha);
                    drawBlob(gc, 100+Math.sin(t*.2)*65,  100+Math.cos(t*.25)*55, 190, 0.0,0.961,1.0,   bgAlpha*0.85);
                    drawBlob(gc,1180+Math.sin(t*.35)*65, 690+Math.cos(t*.3)*45,  220, 1.0,0.176,0.471, bgAlpha*0.75);
                }
                void drawBlob(GraphicsContext g, double x, double y, double r,
                              double rd, double gr, double bl, double al) {
                    g.setFill(new RadialGradient(0,0,.5,.5,.5,true,CycleMethod.NO_CYCLE,
                            new Stop(0,Color.color(rd,gr,bl,al)), new Stop(1,Color.color(rd,gr,bl,0))));
                    g.fillOval(x-r/2,y-r/2,r,r);
                }
            }.start();
            StackPane sp = new StackPane(c);
            sp.setStyle("-fx-background-color: " + bg() + ";");
            return sp;
        }

        // top bar
        private HBox buildTopBar() {
            HBox bar = new HBox(14); bar.setAlignment(Pos.CENTER_LEFT);
            bar.setPadding(new Insets(10, 18, 10, 18));
            bar.setStyle("-fx-background-color:" + topBarBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width: 0 0 1 0;");

            StackPane mini = miniOrb();

            Label name = new Label("NOVA");
            name.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 22px; -fx-text-fill: " + (DARK_MODE.get()?"white":"#1a0050") + ";");
            name.setEffect(new DropShadow(10, Color.web("#00f5ff")));

            Label ver = new Label("v" + APP_VERSION);
            ver.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-text-fill: #00f5ff88;");

            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

            Label clock = new Label();
            clock.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: " + purple() + "cc;");
            Timeline clockTl = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                    clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))));
            clockTl.setCycleCount(Animation.INDEFINITE); clockTl.play();
            clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            Button cmdBtn   = neonButton("⌘ Command Palette", purple());
            cmdBtn.setOnAction(e -> showCommandPalette());
            Button themeBtn = buildThemeToggle();

            Button clsBtn = circleBtn("#ff5f57");
            Button minBtn = circleBtn("#ffbd2e");
            Button maxBtn = circleBtn("#28c840");
            clsBtn.setOnAction(e -> Platform.exit());
            minBtn.setOnAction(e -> stage.setIconified(true));
            maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
            HBox winCtrl = new HBox(7, clsBtn, minBtn, maxBtn); winCtrl.setAlignment(Pos.CENTER);

            bar.getChildren().addAll(mini, name, ver, spacer, clock, cmdBtn, themeBtn, winCtrl);
            return bar;
        }

        private Button buildThemeToggle() {
            String icon  = DARK_MODE.get() ? "☀ Light" : "● Dark";
            String color = DARK_MODE.get() ? "#ffcc44" : "#9b30ff";
            Button btn = neonButton(icon, color);
            btn.setOnAction(e -> DARK_MODE.set(!DARK_MODE.get()));
            return btn;
        }

        //sidebar
        private VBox buildSidebar() {
            VBox sidebar = new VBox(4); sidebar.setPrefWidth(220);
            sidebar.setPadding(new Insets(16, 10, 16, 10));
            sidebar.setStyle("-fx-background-color:" + panelBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width: 0 1 0 0;");

            String[][] items = {
                    {"◉","Dashboard"}, {"◈","Chat"},    {"⚡","Launch"},
                    {"◇","Commands"},  {"◆","Analytics"},{"○","Settings"} //, {"◎","History"}
            };
            for (String[] it : items) {
                boolean active = activeSection.get().equals(it[1]);
                HBox item = sidebarItem(it[0], it[1], active);
                item.setOnMouseClicked(e -> { activeSection.set(it[1]); switchSection(it[1]); });
                sidebar.getChildren().add(item);
            }

            Region sp = new Region(); VBox.setVgrow(sp, Priority.ALWAYS);
            sidebar.getChildren().addAll(sp, buildUserBadge());

            sidebar.setTranslateX(-220);
            TranslateTransition tt = new TranslateTransition(Duration.millis(550), sidebar);
            tt.setToX(0); tt.setDelay(Duration.millis(300)); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
            return sidebar;
        }

        private void switchSection(String section) {
            VBox newCenter = buildChatSection(section);
            centerContent = newCenter;
            newCenter.setOpacity(0);
            rootPane.setCenter(newCenter);
            FadeTransition ft = new FadeTransition(Duration.millis(200), newCenter);
            ft.setToValue(1); ft.play();
        }

        private VBox buildChatSection(String section) {
            return switch (section) {
                case "Chat"      -> buildChatArea();
                case "Launch"    -> buildLaunchView();
                case "Commands"  -> buildCommandsView();
                case "Analytics" -> buildAnalyticsView();
                case "Settings"  -> buildSettingsView();
//                case "History"   -> buildHistoryView();
                default          -> buildDashboardView();
            };
        }

        private HBox sidebarItem(String icon, String label, boolean active) {
            HBox item = new HBox(12); item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(10, 14, 10, 14)); item.setCursor(Cursor.HAND);

            String activeBg  = "-fx-background-color:" + purple() + "1e; -fx-background-radius:8; -fx-border-color:" + purple() + "55; -fx-border-width:0 0 0 2; -fx-border-radius:8;";
            String normalBg  = "-fx-background-radius:8;";
            String hoverBg   = "-fx-background-color:" + purple() + "28; -fx-background-radius:8; -fx-border-color:" + purple() + "66; -fx-border-width:0 0 0 2; -fx-border-radius:8;";

            if (active) item.setStyle(activeBg);
            else        item.setStyle(normalBg);

            Label ico = new Label(icon);
            ico.setStyle("-fx-text-fill:" + (active ? purple() : textDim()) + "; -fx-font-size:16px;");

            Label lbl = new Label(label);
            lbl.setStyle(fontMain() + " -fx-text-fill:" + (active ? textMain() : textDim()) + ";");

            item.getChildren().addAll(ico, lbl);

            if (!active) {
                item.setOnMouseEntered(e -> {
                    item.setStyle(hoverBg);
                    ico.setStyle("-fx-text-fill:" + purple() + "; -fx-font-size:16px;");
                    ico.setEffect(new DropShadow(8, Color.web(purple())));
                    lbl.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), item);
                    st.setToX(1.03); st.setToY(1.03); st.play();
                });
                item.setOnMouseExited(e -> {
                    item.setStyle(normalBg);
                    ico.setStyle("-fx-text-fill:" + textDim() + "; -fx-font-size:16px;");
                    ico.setEffect(null);
                    lbl.setStyle(fontMain() + " -fx-text-fill:" + textDim() + ";");
                    ScaleTransition st = new ScaleTransition(Duration.millis(120), item);
                    st.setToX(1.0); st.setToY(1.0); st.play();
                });
            } else {
                ico.setEffect(new DropShadow(12, Color.web(purple())));
            }

            return item;
        }

        private VBox buildUserBadge() {
            VBox badge = new VBox(4);
            badge.setStyle("-fx-background-color:" + purple() + "18; -fx-background-radius:10; -fx-border-color:" + purple() + "33; -fx-border-radius:10; -fx-padding:10 12 10 12;");
            Circle avatar = new Circle(18, Color.web(purple() + "44"));
            avatar.setStroke(Color.web(purple())); avatar.setStrokeWidth(1.5);
            Label aLbl = new Label("U"); aLbl.setStyle("-fx-text-fill:" + purple() + "; -fx-font-size:14px; -fx-font-weight:bold;");
            Label uName   = new Label("USER"); uName.setStyle(fontMono() + " -fx-text-fill:" + textMain() + ";");
            Label uStatus = new Label("● Online"); uStatus.setStyle("-fx-font-size:11px; -fx-text-fill:" + cyan() + ";");
            HBox row = new HBox(10, new StackPane(avatar, aLbl), new VBox(2, uName, uStatus));
            row.setAlignment(Pos.CENTER_LEFT);
            badge.getChildren().add(row);
            return badge;
        }

        // launch webs/apps
        private VBox buildLaunchView() {
            VBox view = new VBox(0);
            HBox header = sectionHeader("⚡  LAUNCH", "Open Websites & Apps");
            VBox inner = new VBox(24); inner.setPadding(new Insets(24));

            Label webTitle = new Label("WEB  ·  SITES & SERVICES");
            webTitle.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + ";");

            FlowPane webGrid = new FlowPane(10, 10);
            webGrid.setPrefWrapLength(700);

            String[][] featuredSites = {
                    {"🌐","Google",      "google"},
                    {"▶","YouTube",     "youtube"},
                    {"✉","Gmail",       "gmail"},
                    {"🐙","GitHub",      "github"},
                    {"📘","Facebook",    "facebook"},
                    {"📸","Instagram",   "instagram"},
                    {"🐦","Twitter/X",   "twitter"},
                    {"💬","WhatsApp",    "whatsapp"},
                    {"🔴","Reddit",      "reddit"},
                    {"🎬","Netflix",     "netflix"},
                    {"🎵","Spotify",     "spotify"},
                    {"🛒","Amazon",      "amazon"},
                    {"💼","LinkedIn",    "linkedin"},
                    {"🗺","Maps",        "maps"},
                    {"🔤","Translate",   "translate"},
                    {"📂","Drive",       "drive"},
            };

            for (String[] site : featuredSites) {
                VBox tile = buildWebTile(site[0], site[1], site[2]);
                webGrid.getChildren().add(tile);
            }

            HBox urlBar = buildCustomUrlBar();

            Label appsTitle = new Label("APPS  ·  SYSTEM APPLICATIONS");
            appsTitle.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + ";");

            FlowPane appGrid = new FlowPane(10, 10);
            appGrid.setPrefWrapLength(700);

            for (AppLauncher.AppEntry app : AppLauncher.getApps()) {
                VBox tile = buildAppTile(app);
                appGrid.getChildren().add(tile);
            }

            inner.getChildren().addAll(webTitle, webGrid, urlBar, appsTitle, appGrid);
            ScrollPane scroll = styledScroll(inner);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            view.getChildren().addAll(header, scroll);
            return view;
        }

        private VBox buildWebTile(String emoji, String name, String siteKey) {
            String url = WebOpener.getSiteMap().get(siteKey);
            VBox tile = new VBox(6); tile.setAlignment(Pos.CENTER);
            tile.setPrefSize(90, 80); tile.setCursor(Cursor.HAND);
            tile.setPadding(new Insets(12, 8, 10, 8));
            tile.setStyle("-fx-background-color:" + cyan() + "0d; -fx-background-radius:12; -fx-border-color:" + cyan() + "55; -fx-border-radius:12; -fx-border-width:1;");

            Label emo = new Label(emoji); emo.setStyle("-fx-font-size:22px;");
            Label lbl = new Label(name);  lbl.setStyle(fontDim() + " -fx-text-fill:" + textMain() + "; -fx-font-weight:bold;");
            lbl.setWrapText(true); lbl.setTextAlignment(TextAlignment.CENTER);

            tile.getChildren().addAll(emo, lbl);
            tile.setOnMouseClicked(e -> {
                WebOpener.openUrl(url);
                showToast("Opening " + name + "...");
            });
            tile.setOnMouseEntered(e -> {
                tile.setStyle("-fx-background-color:" + cyan() + "22; -fx-background-radius:12; -fx-border-color:" + cyan() + "aa; -fx-border-radius:12; -fx-border-width:1.5;");
                tile.setEffect(new DropShadow(12, Color.web(cyan())));
                ScaleTransition st = new ScaleTransition(Duration.millis(130), tile);
                st.setToX(1.07); st.setToY(1.07); st.play();
            });
            tile.setOnMouseExited(e -> {
                tile.setStyle("-fx-background-color:" + cyan() + "0d; -fx-background-radius:12; -fx-border-color:" + cyan() + "55; -fx-border-radius:12; -fx-border-width:1;");
                tile.setEffect(null);
                ScaleTransition st = new ScaleTransition(Duration.millis(130), tile);
                st.setToX(1.0); st.setToY(1.0); st.play();
            });
            return tile;
        }

        private VBox buildAppTile(AppLauncher.AppEntry app) {
            VBox tile = new VBox(6); tile.setAlignment(Pos.CENTER);
            tile.setPrefSize(100, 84); tile.setCursor(Cursor.HAND);
            tile.setPadding(new Insets(12, 8, 10, 8));
            tile.setStyle("-fx-background-color:" + app.color() + "0d; -fx-background-radius:12; -fx-border-color:" + app.color() + "55; -fx-border-radius:12; -fx-border-width:1;");

            Label emo = new Label(app.icon()); emo.setStyle("-fx-font-size:22px;");
            Label lbl = new Label(app.name());
            lbl.setStyle(fontDim() + " -fx-text-fill:" + textMain() + "; -fx-font-weight:bold;");
            lbl.setWrapText(true); lbl.setTextAlignment(TextAlignment.CENTER);
            Label desc = new Label(app.desc());
            desc.setStyle("-fx-font-size:10px; -fx-text-fill:" + textDim() + ";");
            desc.setWrapText(true); desc.setTextAlignment(TextAlignment.CENTER);

            tile.getChildren().addAll(emo, lbl, desc);
            tile.setOnMouseClicked(e -> {
                app.action().run();
                showToast("Launching " + app.name() + "...");
            });
            String col = app.color();
            tile.setOnMouseEntered(e -> {
                tile.setStyle("-fx-background-color:" + col + "22; -fx-background-radius:12; -fx-border-color:" + col + "88; -fx-border-radius:12; -fx-border-width:1.5;");
                tile.setEffect(new DropShadow(12, Color.web(col)));
                ScaleTransition st = new ScaleTransition(Duration.millis(130), tile);
                st.setToX(1.07); st.setToY(1.07); st.play();
            });
            tile.setOnMouseExited(e -> {
                tile.setStyle("-fx-background-color:" + col + "0d; -fx-background-radius:12; -fx-border-color:" + col + "55; -fx-border-radius:12; -fx-border-width:1;");
                tile.setEffect(null);
                ScaleTransition st = new ScaleTransition(Duration.millis(130), tile);
                st.setToX(1.0); st.setToY(1.0); st.play();
            });
            return tile;
        }

        private HBox buildCustomUrlBar() {
            HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
            bar.setPadding(new Insets(14, 16, 14, 16));
            bar.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:12; -fx-border-color:" + glassBorder() + "; -fx-border-radius:12; -fx-border-width:1;");

            Label ico = new Label("🌐"); ico.setStyle("-fx-font-size:18px;");
            TextField urlField = new TextField();
            urlField.setPromptText("Enter URL or type a site name (e.g. github.com)...");
            urlField.setStyle("-fx-background-color:" + inputBg() + "; -fx-background-radius:8; -fx-border-color:" + purple() + "44; -fx-border-radius:8; -fx-border-width:1; -fx-text-fill:" + textMain() + "; -fx-prompt-text-fill:" + textDim() + "; " + fontMain() + " -fx-padding:8 14 8 14;");
            HBox.setHgrow(urlField, Priority.ALWAYS);

            Button goBtn = neonButton("Go ▶", cyan());
            Runnable goAction = () -> {
                String txt = urlField.getText().trim();
                if (!txt.isEmpty()) {
                    String url = txt.startsWith("http") ? txt : "https://" + txt;
                    WebOpener.openUrl(url);
                    showToast("Opening " + txt + "...");
                    urlField.clear();
                }
            };
            goBtn.setOnAction(e -> goAction.run());
            urlField.setOnAction(e -> goAction.run());

            bar.getChildren().addAll(ico, urlField, goBtn);
            return bar;
        }

        private void showToast(String message) {
            StackPane sceneRoot = (StackPane) stage.getScene().getRoot();
            Label toast = new Label("  " + message + "  ");
            toast.setStyle("-fx-background-color:" + purple() + "dd; -fx-background-radius:20; " +
                    "-fx-text-fill:white; " + fontMain() + " -fx-font-weight:bold; -fx-padding:8 18 8 18;");
            toast.setEffect(new DropShadow(16, Color.web(purple())));
            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            StackPane.setMargin(toast, new Insets(0, 0, 40, 0));
            sceneRoot.getChildren().add(toast);
            toast.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
            fadeIn.setToValue(1); fadeIn.play();
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> sceneRoot.getChildren().remove(toast));
                fadeOut.play();
            });
            pause.play();
        }

        // dashboard
        private VBox buildDashboardView() {
            VBox view = new VBox(0);
            HBox header = sectionHeader("◉  DASHBOARD", "System Overview");
            VBox inner = new VBox(20); inner.setPadding(new Insets(24));

            HBox stats = new HBox(16);
            stats.getChildren().addAll(
                    bigStatCard("CPU USAGE", "23%",    purple()),
                    bigStatCard("MEMORY",    "41%",    purple()),
                    bigStatCard("NETWORK",   "STABLE", purple()),
                    bigStatCard("UPTIME",    "14:22",  "#ff2d78")
            );

            Label recentTitle = new Label("RECENT ACTIVITY");
            recentTitle.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + ";");

            VBox activities = new VBox(6);
            String[][] acts = {
                    {"◎","Chat session started","2 min ago",purple()},
                    {"◇","Opened YouTube via web launcher","5 min ago",purple()},
                    {"◉","System check passed","12 min ago","#28c840"},
                    {"◆","Analytics refreshed","1 hr ago",purple()},
            };
            for (String[] a : acts) {
                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10,14,10,14));
                row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:8; -fx-border-color:" + glassBorder() + "; -fx-border-radius:8; -fx-border-width:1;");
                Label ic = new Label(a[0]); ic.setStyle("-fx-text-fill:" + a[3] + "; -fx-font-size:15px;");
                Label tx = new Label(a[1]); tx.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label ts = new Label(a[2]); ts.setStyle(fontMono() + " -fx-text-fill:" + textDim() + ";");
                row.getChildren().addAll(ic, tx, sp, ts);
                activities.getChildren().add(row);
            }

            ScrollPane scroll = styledScroll(inner);
            inner.getChildren().addAll(stats, recentTitle, activities);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            view.getChildren().addAll(header, scroll);
            return view;
        }

        private VBox bigStatCard(String label, String value, String color) {
            VBox card = new VBox(6); card.setPadding(new Insets(16, 18, 16, 18)); HBox.setHgrow(card, Priority.ALWAYS);
            card.setStyle("-fx-background-color:" + color + "0d; -fx-background-radius:12; -fx-border-color:" + color + "44; -fx-border-radius:12; -fx-border-width:1;");
            Label lbl = new Label(label); lbl.setStyle(fontLabel() + " -fx-text-fill:" + color + "99;");
            Label val = new Label(value);
            val.setStyle("-fx-font-family:'Courier New'; -fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
            val.setEffect(new DropShadow(10, Color.web(color)));
            card.getChildren().addAll(lbl, val);
            return card;
        }

        //chat box
        private VBox buildChatArea() {
            VBox area = new VBox(0);
            VBox.setVgrow(area,Priority.ALWAYS);
            area.setMaxHeight(Double.MAX_VALUE);
            HBox header = sectionHeader("◈  CHAT", "NOVA Chat Interface  ● ONLINE");

            StackPane chatBody = new StackPane();
            VBox.setVgrow(chatBody, Priority.ALWAYS);


            Canvas ringsBg = buildFloatingRingsBackground();
            ringsBg.setMouseTransparent(true);
           // canvas binding to chat area
            ringsBg.widthProperty().bind(chatBody.widthProperty());
            ringsBg.heightProperty().bind(chatBody.heightProperty());

            VBox messages = new VBox(14); messages.setPadding(new Insets(20));
            messages.setStyle("-fx-background-color: transparent;");

            addMessage(messages, "NOVA",
                    "Welcome back! I'm NOVA v" + APP_VERSION + ". You can ask me to open websites or apps — try:\n" +
                            "• \"open youtube\"\n• \"can u open github\"\n• \"launch calculator\"\n• \"take me to gmail\"", false);
            addMessage(messages, "USER", "Show me today's system status.", true);
            addMessage(messages, "NOVA",
                    "All systems nominal. CPU 23%, Memory 41%, Network: stable. Web & App launchers are active. Dark mode: "
                            +(DARK_MODE.get()?"enabled":"disabled")+".", false);

            ScrollPane scroll = new ScrollPane(messages);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
            scroll.setMaxWidth(Double.MAX_VALUE);
            scroll.setMaxHeight(Double.MAX_VALUE);
            StackPane.setAlignment(scroll,Pos.TOP_LEFT);
            scroll.prefHeightProperty().bind(chatBody.heightProperty());

            chatBody.getChildren().addAll(ringsBg, scroll);

            HBox inputBar = buildInputBar(messages, scroll);
            area.getChildren().addAll(header, chatBody, inputBar);
            return area;
        }

        private Canvas buildFloatingRingsBackground() {
            Canvas canvas = new Canvas();

            Random rng = new Random(77);
            final int N = 40; // number of circles

            // per-circle state
            double[] cx   = new double[N];
            double[] cy   = new double[N];
            double[] cvx  = new double[N];
            double[] cvy  = new double[N];
            double[] crad = new double[N];  // radius: 5–28 px (small circles)
            double[] csw  = new double[N];  // stroke width: 1–2 px
            double[] cpha = new double[N];  // alpha phase
            double[] cfrq = new double[N];  // alpha pulse freq
            int[]    ctyp = new int[N];     // 0=purple, 1=cyan, 2=magenta

            for (int i = 0; i < N; i++) {
                cx[i]   = rng.nextDouble() * 900;
                cy[i]   = rng.nextDouble() * 700;
                double spd = 0.6 + rng.nextDouble() * 1.0;
                double ang = rng.nextDouble() * Math.PI * 2;
                cvx[i]  = Math.cos(ang) * spd;
                cvy[i]  = Math.sin(ang) * spd;
                crad[i] = 5 + rng.nextDouble() * 23;
                csw[i]  = 1.5 + rng.nextDouble() * 1.0;
                cpha[i] = rng.nextDouble() * Math.PI * 2;
                cfrq[i] = 0.5 + rng.nextDouble() * 1.0;
                ctyp[i] = rng.nextInt(3);
            }

            GraphicsContext gc = canvas.getGraphicsContext2D();

            new AnimationTimer() {
                double t = 0;
                @Override public void handle(long now) {
                    t += 0.016;
                    double W = canvas.getWidth()  > 10 ? canvas.getWidth()  : 900;
                    double H = canvas.getHeight() > 10 ? canvas.getHeight() : 600;

                    gc.clearRect(0, 0, W, H);

                    for (int i = 0; i < N; i++) {
                        // float
                        cx[i] += cvx[i]; cy[i] += cvy[i];
                        if (cx[i] < -crad[i]*2) { cvx[i] =  Math.abs(cvx[i]); }
                        if (cx[i] > W+crad[i]*2){ cvx[i] = -Math.abs(cvx[i]); }
                        if (cy[i] < -crad[i]*2) { cvy[i] =  Math.abs(cvy[i]); }
                        if (cy[i] > H+crad[i]*2){ cvy[i] = -Math.abs(cvy[i]); }


                        double alpha = 0.06 + 0.12 * (0.5 + 0.5 * Math.sin(t * cfrq[i] + cpha[i]));
                        if (!DARK_MODE.get()) alpha *= 0.8;

                        Color col = switch (ctyp[i]) {
                            case 0 -> Color.color(0.608, 0.188, 1.000, alpha); // purple
                            case 1 -> Color.color(0.000, 0.961, 1.000, alpha); // cyan
                            default -> Color.color(1.000, 0.176, 0.471, alpha); // magenta
                        };

                        gc.setStroke(col);
                        gc.setLineWidth(csw[i]);
                        gc.strokeOval(cx[i] - crad[i], cy[i] - crad[i], crad[i] * 2, crad[i] * 2);
                    }
                }
            }.start();

            return canvas;
        }

        private void addMessage(VBox container, String who, String text, boolean isUser) {
            String col = isUser ? purple() : cyan();
            VBox msg = new VBox(4); msg.setPadding(new Insets(12,16,12,16)); msg.setMaxWidth(560);
            msg.setStyle("-fx-background-color:" + col + (isUser?"2a":"0f") + "; -fx-background-radius:" + (isUser?"14 14 4 14":"14 14 14 4") + "; -fx-border-color:" + col + (isUser?"55":"33") + "; -fx-border-radius:" + (isUser?"14 14 4 14":"14 14 14 4") + "; -fx-border-width:1;");
            Label whoLbl = new Label(who + "  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
            whoLbl.setStyle(fontMono() + " -fx-font-weight:bold; -fx-text-fill:" + col + ";");
            Label txtLbl = new Label(text); txtLbl.setWrapText(true);
            txtLbl.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
            msg.getChildren().addAll(whoLbl, txtLbl);
            HBox row = new HBox(msg); row.setPadding(new Insets(2,0,2,0));
            row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            msg.setOpacity(0);
            container.getChildren().add(row);
            FadeTransition ft = new FadeTransition(Duration.millis(350), msg);
            ft.setToValue(1); ft.play();
        }

        private HBox buildInputBar(VBox messages, ScrollPane scroll) {
            HBox bar = new HBox(10); bar.setPadding(new Insets(12, 16, 14, 16)); bar.setAlignment(Pos.CENTER);
            bar.setStyle("-fx-background-color:" + panelBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width:1 0 0 0;");

            VBox.setVgrow(bar,Priority.NEVER);

            TextField input = new TextField();
            input.setPromptText("Ask NOVA anything, or type 'open youtube' / 'launch calculator'...");
            input.setStyle("-fx-background-color:" + inputBg() + "; -fx-background-radius:22; -fx-border-color:" + purple() + "44; -fx-border-radius:22; -fx-border-width:1; -fx-text-fill:" + textMain() + "; -fx-prompt-text-fill:" + textDim() + "; " + fontMain() + " -fx-padding:10 18 10 18;");
            HBox.setHgrow(input, Priority.ALWAYS);

            Button send = neonButton("Send ▶", cyan());
            Runnable doSend = () -> {
                String txt = input.getText().trim();
                if (!txt.isEmpty()) {
                    addMessage(messages, "USER", txt, true);
                    input.clear();
                    String response = generateResponse(txt);
                    PauseTransition pt = new PauseTransition(Duration.millis(650));
                    pt.setOnFinished(e -> {
                        addMessage(messages, "NOVA", response, false);
                        scroll.setVvalue(1.0);
                    });
                    pt.play();
                    scroll.setVvalue(1.0);
                }
            };
            send.setOnAction(e -> doSend.run());
            input.setOnAction(e -> doSend.run());
            bar.getChildren().addAll(input, send);
            return bar;
        }

        private String generateResponse(String input) {
            String lower = input.toLowerCase();
            String appResponse = AppLauncher.handle(input);
            if (appResponse != null) return appResponse;
            String webResponse = WebOpener.handle(input);
            if (webResponse != null) return webResponse;
            if (lower.contains("hello") || lower.contains("hi"))
                return "Hello! Neural core active. I can open websites and apps too — try \"open youtube\" or \"launch calculator\"!";
            if (lower.contains("time"))
                return "Current time: " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ".";
            if (lower.contains("date"))
                return "Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd yyyy")) + ".";
            if (lower.contains("dark") || lower.contains("light") || lower.contains("theme"))
                return "Theme is currently " + (DARK_MODE.get()?"DARK":"LIGHT") + ". Use the top bar toggle to switch.";
            if (lower.contains("status") || lower.contains("system"))
                return "All systems nominal. CPU 23%, Memory 41%, Network: " + NET_LABEL.get() + ". Neural core fully operational.";
            if (lower.contains("network") || lower.contains("ping") || lower.contains("internet"))
                return "Live network: " + NET_LABEL.get() + (NET_REACHABLE.get() ? " — connection good." : " — no route to host.");
            if (lower.contains("version"))
                return "Running NOVA v" + APP_VERSION + " — " + APP_TAGLINE + ".";
            if (lower.contains("help") || lower.contains("what can you do"))
                return "I can:\n• Open websites: \"open youtube\", \"go to github\", \"can u open gmail\"\n• Launch apps: \"launch calculator\", \"open notepad\"\n• Check system status, time, date\n• Toggle dark/light mode\n• Search Google for anything!";
            if (lower.contains("website") || lower.contains("sites"))
                return "Supported sites: " + String.join(", ", WebOpener.getSiteMap().keySet()) + ". Just say \"open [site name]\"!";
            return "Processing: \"" + input + "\" — Try saying \"open youtube\", \"launch calculator\", or \"help\" to see what I can do.";
        }

        // COMPONENTS VIEW
        private VBox buildCommandsView() {
            VBox view = new VBox(0);
            HBox header = sectionHeader("◇  COMMANDS", "Available Commands");
            VBox inner = new VBox(10); inner.setPadding(new Insets(20));

            Object[][] cmds = {
                    {"⌘", "New Conversation",    "Start a fresh chat session",            "Ctrl+N",  (Runnable)() -> { activeSection.set("Chat");      switchSection("Chat"); }},
                    {"⚡", "Open Website",        "Say: open youtube / go to github",      "Chat",    null},
                    {"🚀", "Launch App",          "Say: launch calculator / open notepad", "Chat",    null},
                    {"◉", "System Check",        "Run full system diagnostics",           "Ctrl+D",  (Runnable)() -> { activeSection.set("Analytics"); switchSection("Analytics"); }},
                    {"◈", "Toggle Theme",        "Switch between Dark and Light mode",    "Ctrl+T",  (Runnable)() -> DARK_MODE.set(!DARK_MODE.get())},
                    {"◎", "Clear History",       "Clear all chat history",                "Ctrl+L",  (Runnable)() -> { activeSection.set("Chat"); switchSection("Chat"); }},
                    {"○", "Preferences",         "Open settings panel",                   "Ctrl+,",  (Runnable)() -> { activeSection.set("Settings"); switchSection("Settings"); }},
                    {"◇", "Command Palette",     "Open quick command search",             "Ctrl+P",  (Runnable)() -> showCommandPalette()},
                    {"⌂", "Dashboard",           "Return to main dashboard",              "Ctrl+H",  (Runnable)() -> { activeSection.set("Dashboard"); switchSection("Dashboard"); }},
            };

            for (Object[] cmd : cmds) {
                String cmdIcon = (String)cmd[0];
                String cmdName = (String)cmd[1];
                String cmdDesc = (String)cmd[2];
                String cmdKey  = (String)cmd[3];
                Runnable action = (Runnable)cmd[4];

                HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12,16,12,16));
                row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:10; -fx-border-color:" + glassBorder() + "; -fx-border-radius:10; -fx-border-width:1;");

                if (action != null) row.setCursor(Cursor.HAND);

                Label ic = new Label(cmdIcon);
                ic.setStyle("-fx-font-size:18px; -fx-text-fill:" + cyan() + "; -fx-min-width:26;");

                VBox info = new VBox(3);
                Label title = new Label(cmdName);
                title.setStyle(fontMain() + " -fx-text-fill:" + textMain() + "; -fx-font-weight:bold;");
                Label desc  = new Label(cmdDesc);
                desc.setStyle(fontDim() + " -fx-text-fill:" + textDim() + ";");
                info.getChildren().addAll(title, desc);

                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

                Label kbd = new Label(cmdKey);
                kbd.setStyle(fontMono() + " -fx-text-fill:" + purple() + "; -fx-background-color:" + purple() + "1a; -fx-background-radius:4; -fx-border-color:" + purple() + "33; -fx-border-radius:4; -fx-border-width:1; -fx-padding:3 7 3 7;");

                row.getChildren().addAll(ic, info, sp, kbd);

                row.setOnMouseEntered(e -> {
                    row.setStyle("-fx-background-color:" + purple() + "1a; -fx-background-radius:10; -fx-border-color:" + purple() + "66; -fx-border-radius:10; -fx-border-width:1;");
                    ic.setEffect(new DropShadow(8, Color.web(purple())));
                });
                row.setOnMouseExited(e -> {
                    row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:10; -fx-border-color:" + glassBorder() + "; -fx-border-radius:10; -fx-border-width:1;");
                    ic.setEffect(null);
                });

                if (action != null) {
                    final Runnable act = action;
                    row.setOnMouseClicked(e -> act.run());
                }

                inner.getChildren().add(row);
            }

            ScrollPane scroll = styledScroll(inner);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            view.getChildren().addAll(header, scroll);
            return view;
        }

        // Analytics view
        private VBox buildAnalyticsView() {
            VBox view = new VBox(0);
            HBox header = sectionHeader("◆  ANALYTICS", "Usage Statistics");
            VBox inner = new VBox(18); inner.setPadding(new Insets(20));

            Label chartTitle = new Label("MESSAGES PER DAY (LAST 7 DAYS)");
            chartTitle.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + ";");

            HBox chart = new HBox(10); chart.setAlignment(Pos.BOTTOM_LEFT);
            chart.setPadding(new Insets(16));
            chart.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:12; -fx-border-color:" + glassBorder() + "; -fx-border-radius:12; -fx-border-width:1;");
            chart.setMinHeight(160);

            String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
            int[] vals = {12, 19, 8, 25, 17, 30, 14};
            String[] colors = {purple(), purple(), purple(), purple(), purple(), "#ff2d78", purple()};

            for (int i = 0; i < 7; i++) {
                VBox bar = new VBox(4); bar.setAlignment(Pos.BOTTOM_CENTER);
                int finalI = i;
                Rectangle rect = new Rectangle(28, 0);
                rect.setFill(Color.web(colors[i])); rect.setArcWidth(5); rect.setArcHeight(5);
                rect.setEffect(new DropShadow(8, Color.web(colors[i])));
                new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(rect.heightProperty(), 0)),
                        new KeyFrame(Duration.millis(600 + finalI * 80), new KeyValue(rect.heightProperty(), (double)vals[i]/30*110, Interpolator.EASE_OUT))
                ).play();
                Label dayLbl = new Label(days[i]); dayLbl.setStyle(fontMono() + " -fx-text-fill:" + textDim() + ";");
                Label valLbl = new Label(String.valueOf(vals[i])); valLbl.setStyle(fontMono() + " -fx-text-fill:" + colors[i] + ";");
                bar.getChildren().addAll(valLbl, rect, dayLbl);
                chart.getChildren().add(bar);
            }

            HBox summary = new HBox(16);
            summary.getChildren().addAll(
                    bigStatCard("TOTAL MSGS","125",  purple()),
                    bigStatCard("AVG/DAY",   "17.8", purple()),
                    bigStatCard("UPTIME",    "99.9%","#28c840"),
                    bigStatCard("COMMANDS",  "48",   "#ff2d78")
            );

            inner.getChildren().addAll(chartTitle, chart, summary);
            ScrollPane scroll = styledScroll(inner);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            view.getChildren().addAll(header, scroll);
            return view;
        }

        // Settings view
        private VBox buildSettingsView() {
            VBox view = new VBox(0);
            HBox header = sectionHeader("○  SETTINGS", "Preferences");
            VBox inner = new VBox(16); inner.setPadding(new Insets(20));

            inner.getChildren().add(settingRow("◈","Dark Mode","Toggle between dark and light interface theme", buildToggleSwitch(DARK_MODE.get(), val -> DARK_MODE.set(val))));
            inner.getChildren().add(settingRow("◉","Notifications","Enable desktop notifications for messages", buildToggleSwitch(true, v -> {})));
            inner.getChildren().add(settingRow("○","Font Size","Adjust the interface font size (12–20px)",
                    buildLiveFontSizeSlider()));

            VBox about = new VBox(6); about.setPadding(new Insets(16));
            about.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:10; -fx-border-color:" + glassBorder() + "; -fx-border-radius:10; -fx-border-width:1;");
            Label aTitle = new Label("ABOUT NOVA"); aTitle.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + ";");
            Label aVer   = new Label("Version " + APP_VERSION + " — " + APP_TAGLINE); aVer.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
            Label aBuild = new Label("Build: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); aBuild.setStyle(fontMono() + " -fx-text-fill:" + textDim() + ";");
            about.getChildren().addAll(aTitle, aVer, aBuild);
            inner.getChildren().add(about);

            ScrollPane scroll = styledScroll(inner);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            view.getChildren().addAll(header, scroll);
            return view;
        }

        private HBox buildLiveFontSizeSlider() {
            HBox container = new HBox(10); container.setAlignment(Pos.CENTER_LEFT);
            Slider sl = new Slider(12, 20, FONT_SIZE.get()); sl.setPrefWidth(110);
            sl.setStyle("-fx-control-inner-background:" + panelBorder() + "; -fx-accent:" + purple() + ";");
            sl.setSnapToTicks(true); sl.setMajorTickUnit(2); sl.setMinorTickCount(1);

            Label sizeLbl = new Label((int)FONT_SIZE.get() + "px");
            sizeLbl.setStyle(fontMono() + " -fx-text-fill:" + purple() + "; -fx-min-width:38;");

            sl.valueProperty().addListener((obs, oldV, newV) -> {
                double rounded = Math.round(newV.doubleValue());
                FONT_SIZE.set(rounded);
                sizeLbl.setText((int)rounded + "px");
            });

            container.getChildren().addAll(sl, sizeLbl);
            return container;
        }

        private HBox settingRow(String icon, String name, String desc, Node control) {
            HBox row = new HBox(14); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14,16,14,16));
            row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:10; -fx-border-color:" + glassBorder() + "; -fx-border-radius:10; -fx-border-width:1;");
            Label ic = new Label(icon); ic.setStyle("-fx-font-size:16px; -fx-text-fill:" + purple() + "; -fx-min-width:22;");
            VBox info = new VBox(3);
            Label n = new Label(name); n.setStyle(fontMain() + " -fx-text-fill:" + textMain() + "; -fx-font-weight:bold;");
            Label d = new Label(desc); d.setStyle(fontDim() + " -fx-text-fill:" + textDim() + ";");
            info.getChildren().addAll(n, d);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            row.getChildren().addAll(ic, info, sp, control);
            return row;
        }

        private StackPane buildToggleSwitch(boolean initialOn, java.util.function.Consumer<Boolean> onChange) {
            BooleanProperty on = new SimpleBooleanProperty(initialOn);
            double W = 44, H = 24;
            Rectangle track = new Rectangle(W, H); track.setArcWidth(H); track.setArcHeight(H);
            track.setFill(on.get() ? Color.web(purple()) : Color.web(panelBorder()));
            Circle thumb = new Circle(H/2 - 2); thumb.setFill(Color.WHITE);
            thumb.setTranslateX(on.get() ? W/2 - H/2 + 2 : -(W/2 - H/2 + 2));
            StackPane sw = new StackPane(track, thumb);
            sw.setPrefSize(W, H); sw.setMaxSize(W, H); sw.setCursor(Cursor.HAND);
            sw.setOnMouseClicked(e -> {
                on.set(!on.get());
                track.setFill(on.get() ? Color.web(purple()) : Color.web(panelBorder()));
                TranslateTransition tt = new TranslateTransition(Duration.millis(180), thumb);
                tt.setToX(on.get() ? W/2 - H/2 + 2 : -(W/2 - H/2 + 2)); tt.play();
                onChange.accept(on.get());
            });
            return sw;
        }

        //History view
//        private VBox buildHistoryView() {
//            VBox view = new VBox(0);
//            HBox header = sectionHeader("◎  HISTORY", "Conversation History");
//            VBox inner = new VBox(8); inner.setPadding(new Insets(20));
//
//            String[][] history = {
//                    {"Today",      "Opened YouTube via NOVA",        "Just now"},
//                    {"Today",      "System status check",            "5 min ago"},
//                    {"Today",      "Dark mode discussion",           "23 min ago"},
//                    {"Yesterday",  "Launched Calculator app",        "1 day ago"},
//                    {"Yesterday",  "Analytics review",               "1 day ago"},
//                    {"2 days ago", "Initial NOVA setup",             "2 days ago"},
//            };
//
//            String lastGroup = "";
//            for (String[] h : history) {
//                if (!h[0].equals(lastGroup)) {
//                    Label grpLbl = new Label(h[0].toUpperCase());
//                    grpLbl.setStyle(fontLabel() + " -fx-text-fill:" + textDim() + "; -fx-padding:8 0 4 0;");
//                    inner.getChildren().add(grpLbl);
//                    lastGroup = h[0];
//                }
//                HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
//                row.setPadding(new Insets(11,14,11,14)); row.setCursor(Cursor.HAND);
//                row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:8; -fx-border-color:" + glassBorder() + "; -fx-border-radius:8; -fx-border-width:1;");
//                Label ic  = new Label("◎"); ic.setStyle("-fx-text-fill:" + cyan() + "; -fx-font-size:14px;");
//                Label tx  = new Label(h[1]); tx.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
//                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
//                Label ts  = new Label(h[2]); ts.setStyle(fontMono() + " -fx-text-fill:" + textDim() + ";");
//                row.getChildren().addAll(ic, tx, sp, ts);
//                row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:" + purple() + "1a; -fx-background-radius:8; -fx-border-color:" + purple() + "44; -fx-border-radius:8; -fx-border-width:1;"));
//                row.setOnMouseExited(e -> row.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:8; -fx-border-color:" + glassBorder() + "; -fx-border-radius:8; -fx-border-width:1;"));
//                inner.getChildren().add(row);
//            }
//
//            ScrollPane scroll = styledScroll(inner);
//            VBox.setVgrow(scroll, Priority.ALWAYS);
//            view.getChildren().addAll(header, scroll);
//            return view;
//        }

        //live widgets
        private VBox buildRightPanel() {
            VBox panel = new VBox(12); panel.setPrefWidth(230); panel.setPadding(new Insets(14, 10, 14, 10));
            panel.setStyle("-fx-background-color:" + panelBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width:0 0 0 1;");

            Label title = new Label("LIVE WIDGETS");
            title.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:2px; -fx-text-fill:" + purple() + "cc;");

            panel.getChildren().addAll(title,
                    buildStatCard("CPU",     "23%", cyan()),
                    buildStatCard("MEMORY",  "41%", purple()),
                    buildLiveNetworkCard(),       // ← replaced static card
                    buildNeuralCard(),
                    buildDateCard(),
                    buildQuickLaunchCard()
            );

            panel.setTranslateX(230);
            TranslateTransition tt = new TranslateTransition(Duration.millis(550), panel);
            tt.setToX(0); tt.setDelay(Duration.millis(400)); tt.setInterpolator(Interpolator.EASE_OUT); tt.play();
            return panel;
        }

        /**
         * Live NETWORK widget — pings 8.8.8.8 every 4 s in a daemon thread.
         * The value label and its color update on the FX thread via the
         * NET_LABEL / NET_COLOR StringProperties defined at class level.
         *
         * Display rules:
         *   < 80 ms  → green  "XX ms ●"
         *   < 200 ms → amber  "XX ms ●"
         *   ≥ 200 ms → red    "XX ms ●"
         *   offline  → magenta "OFFLINE"
         *   first check → yellow "CHECKING…"
         */
        private VBox buildLiveNetworkCard() {
            VBox card = new VBox(4); card.setPadding(new Insets(11,14,11,14));


            Runnable styleCard = () -> {
                String c = NET_COLOR.get();
                card.setStyle("-fx-background-color:" + c + "0d; -fx-background-radius:10; -fx-border-color:" + c + "55; -fx-border-radius:10; -fx-border-width:1;");
            };
            styleCard.run();

            Label lbl = new Label("NETWORK");
            lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:1px; -fx-text-fill:" + NET_COLOR.get() + "bb;");

            Label val = new Label(NET_LABEL.get());
            val.setStyle("-fx-font-family:'Courier New'; -fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + NET_COLOR.get() + ";");
            val.setEffect(new DropShadow(8, Color.web(NET_COLOR.get())));

            // Sub-label showing raw ping or status
            Label sub = new Label("ping 8.8.8.8");
            sub.setStyle("-fx-font-family:'Courier New'; -fx-font-size:10px; -fx-text-fill:" + textDim() + ";");

            // Blinking dot indicator
            Circle dot = new Circle(4, Color.web(NET_COLOR.get()));
            Timeline blink = new Timeline(
                    new KeyFrame(Duration.ZERO,        new KeyValue(dot.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.millis(700), new KeyValue(dot.opacityProperty(), 0.2))
            );
            blink.setCycleCount(Animation.INDEFINITE);
            blink.setAutoReverse(true);
            blink.play();

            HBox dotRow = new HBox(6, dot, sub);
            dotRow.setAlignment(Pos.CENTER_LEFT);

            card.getChildren().addAll(lbl, val, dotRow);

            // React to live updates
            NET_LABEL.addListener((obs, oldV, newV) -> {
                val.setText(newV);
            });
            NET_COLOR.addListener((obs, oldV, newV) -> {
                Color c = Color.web(newV);
                lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:1px; -fx-text-fill:" + newV + "bb;");
                val.setStyle("-fx-font-family:'Courier New'; -fx-font-size:22px; -fx-font-weight:bold; -fx-text-fill:" + newV + ";");
                val.setEffect(new DropShadow(8, Color.web(newV)));
                dot.setFill(Color.web(newV));
                styleCard.run();
            });

            return card;
        }

        private VBox buildStatCard(String label, String value, String color) {
            VBox card = new VBox(4); card.setPadding(new Insets(11,14,11,14));
            card.setStyle("-fx-background-color:" + color + "0d; -fx-background-radius:10; -fx-border-color:" + color + "55; -fx-border-radius:10; -fx-border-width:1;");
            Label lbl = new Label(label);
            lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:1px; -fx-text-fill:" + color + "bb;");
            Label val = new Label(value);
            val.setStyle("-fx-font-family:'Courier New'; -fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
            val.setEffect(new DropShadow(8, Color.web(color)));
            card.getChildren().addAll(lbl, val);
            return card;
        }

        private VBox buildNeuralCard() {
            VBox card = new VBox(6); card.setPadding(new Insets(11,14,11,14));
            card.setStyle("-fx-background-color:" + glass() + "; -fx-background-radius:10; -fx-border-color:" + glassBorder() + "; -fx-border-radius:10; -fx-border-width:1;");
            Label lbl = new Label("NEURAL ACTIVITY");
            lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:1px; -fx-text-fill:" + purple() + "bb;");
            Canvas spark = new Canvas(195, 38);
            GraphicsContext gc = spark.getGraphicsContext2D();
            double[] vals = new double[20];
            Random rng = new Random(7);
            for (int i = 0; i < 20; i++) vals[i] = 8 + rng.nextDouble() * 22;
            new AnimationTimer() {
                double t = 0;
                @Override public void handle(long now) {
                    t += 0.04;
                    gc.clearRect(0,0,195,38);
                    gc.setStroke(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE,
                            new Stop(0,Color.web(purple())), new Stop(1,Color.web(cyan()))));
                    gc.setLineWidth(1.5); gc.beginPath();
                    for (int i=0; i<20; i++) {
                        double y = 34 - (vals[i] + Math.sin(t+i*0.5)*6);
                        if (i==0) gc.moveTo(i*10, y); else gc.lineTo(i*10, y);
                    }
                    gc.stroke();
                }
            }.start();
            card.getChildren().addAll(lbl, spark);
            return card;
        }

        private VBox buildDateCard() {
            VBox card = new VBox(5); card.setPadding(new Insets(11,14,11,14));
            card.setStyle("-fx-background-color:" + cyan() + "0a; -fx-background-radius:10; -fx-border-color:" + cyan() + "44; -fx-border-radius:10; -fx-border-width:1;");
            Label dateLbl = new Label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")).toUpperCase());
            dateLbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-text-fill:" + cyan() + "cc;");
            dateLbl.setWrapText(true);
            Label mode = new Label(DARK_MODE.get() ? "◉ DARK MODE" : "○ LIGHT MODE");
            mode.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-text-fill:" + purple() + ";");
            Label tag = new Label(APP_TAGLINE);
            tag.setStyle(fontDim() + " -fx-text-fill:" + textDim() + ";"); tag.setWrapText(true);
            card.getChildren().addAll(dateLbl, mode, tag);
            return card;
        }

        private VBox buildQuickLaunchCard() {
            VBox card = new VBox(8); card.setPadding(new Insets(11,14,11,14));
            card.setStyle("-fx-background-color:" + purple() + "0a; -fx-background-radius:10; -fx-border-color:" + purple() + "33; -fx-border-radius:10; -fx-border-width:1;");
            Label lbl = new Label("QUICK LAUNCH");
            lbl.setStyle("-fx-font-family:'Courier New'; -fx-font-size:13px; -fx-letter-spacing:1px; -fx-text-fill:" + purple() + "bb;");

            HBox row1 = new HBox(6);
            HBox row2 = new HBox(6);

            String[][] quick = {
                    {"▶","YT",     "youtube"},
                    {"✉","Mail",   "gmail"},
                    {"🐙","Git",   "github"},
                    {"🔴","Reddit","reddit"},
            };
            boolean first = true;
            for (String[] q : quick) {
                Button btn = new Button(q[0] + " " + q[1]);
                btn.setStyle("-fx-background-color:" + cyan() + "18; -fx-background-radius:8; -fx-border-color:" + cyan() + "66; -fx-border-radius:8; -fx-border-width:1; -fx-text-fill:" + cyan() + "; -fx-font-size:11px; -fx-cursor:hand; -fx-padding:5 8 5 8;");
                String siteKey = q[2];
                btn.setOnAction(e -> {
                    WebOpener.openUrl(WebOpener.getSiteMap().get(siteKey));
                    showToast("Opening " + q[1] + "...");
                });
                btn.setOnMouseEntered(e -> btn.setEffect(new DropShadow(8, Color.web(cyan()))));
                btn.setOnMouseExited(e -> btn.setEffect(null));
                (first ? row1 : row2).getChildren().add(btn);
                first = !first;
            }
            card.getChildren().addAll(lbl, row1, row2);
            return card;
        }

        // dock
        private HBox buildDock() {
            HBox dock = new HBox(6); dock.setAlignment(Pos.CENTER);
            dock.setPadding(new Insets(8, 16, 10, 16));
            dock.setStyle("-fx-background-color:" + panelBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width:1 0 0 0;");

            String[][] dockItems = {
                    {"⌂","Dashboard","Dashboard"}, {"◈","AI","Chat"},
                    {"⚡","Launch","Launch"},       {"⌘","CMD","Commands"},
                    {"◉","Monitor","Analytics"},   {"◇","History","History"},
                    {"○","Prefs","Settings"}
            };
            for (String[] it : dockItems) dock.getChildren().add(dockBtn(it[0], it[1], it[2]));
            return dock;
        }

        private VBox dockBtn(String icon, String label, String section) {
            Label ico = new Label(icon); ico.setStyle("-fx-font-size:18px; -fx-text-fill:" + purple() + "cc;");
            Label lbl = new Label(label); lbl.setStyle(fontDim() + " -fx-text-fill:" + textDim() + ";");
            VBox btn = new VBox(2, ico, lbl); btn.setAlignment(Pos.CENTER);
            btn.setPadding(new Insets(6,14,4,14)); btn.setCursor(Cursor.HAND); btn.setStyle("-fx-background-radius:8;");
            btn.setOnMouseClicked(e -> { activeSection.set(section); switchSection(section); });
            btn.setOnMouseEntered(e -> {
                btn.setStyle("-fx-background-color:" + purple() + "22; -fx-background-radius:8;");
                ico.setEffect(new DropShadow(10, Color.web(purple())));
                ScaleTransition st = new ScaleTransition(Duration.millis(140), btn);
                st.setToX(1.12); st.setToY(1.12); st.play();
            });
            btn.setOnMouseExited(e -> {
                btn.setStyle("-fx-background-radius:8;"); ico.setEffect(null);
                ScaleTransition st = new ScaleTransition(Duration.millis(140), btn);
                st.setToX(1.0); st.setToY(1.0); st.play();
            });
            return btn;
        }

        //command palette
        private void showCommandPalette() {
            Scene scene = stage.getScene();
            StackPane sceneRoot = (StackPane) scene.getRoot();

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: #000000bb;");
            overlay.setPrefSize(1260, 780);

            VBox palette = new VBox(10); palette.setPrefWidth(580); palette.setMaxWidth(580);
            palette.setPadding(new Insets(20));
            palette.setStyle("-fx-background-color:" + (DARK_MODE.get()?"#0d001f":"#f8f4ff") + "; -fx-background-radius:14; -fx-border-color:" + purple() + "66; -fx-border-radius:14; -fx-border-width:1.5;");
            palette.setEffect(new DropShadow(44, Color.web(purple() + "55")));

            Label pt = new Label("⌘  COMMAND PALETTE");
            pt.setStyle(fontLabel() + " -fx-text-fill:" + purple() + "bb;");

            TextField search = new TextField();
            search.setPromptText("Type a command or search...");
            search.setStyle("-fx-background-color:" + inputBg() + "; -fx-background-radius:8; -fx-border-color:" + purple() + "55; -fx-border-radius:8; -fx-border-width:1; -fx-text-fill:" + textMain() + "; -fx-prompt-text-fill:" + textDim() + "; " + fontMain() + " -fx-padding:10 14 10 14;");

            String[][] commands = {
                    {"⌂","Dashboard",         "Go to dashboard",            "Dashboard"},
                    {"◈","New Chat",          "Start a fresh conversation", "Chat"},
                    {"⚡","Launch Panel",      "Open web & app launcher",    "Launch"},
                    {"◉","System Monitor",    "Open analytics panel",       "Analytics"},
                    {"◈","Toggle Dark Mode",  "Switch UI theme",            "theme"},
                    {"○","Settings",          "Open preferences",           "Settings"},
                    {"◎","View History",      "Browse past chats",          "History"},
                    {"◇","Command Center",    "Browse all commands",        "Commands"},
            };

            VBox cmdList = new VBox(4);
            Runnable[] populateList = {null};
            populateList[0] = () -> {
                cmdList.getChildren().clear();
                String filter = search.getText().toLowerCase();
                boolean isFirst = true;
                for (String[] cmd : commands) {
                    if (!filter.isEmpty() && !cmd[1].toLowerCase().contains(filter) && !cmd[2].toLowerCase().contains(filter)) continue;
                    HBox item = new HBox(12); item.setAlignment(Pos.CENTER_LEFT);
                    item.setPadding(new Insets(9,14,9,14)); item.setCursor(Cursor.HAND);
                    item.setStyle((isFirst ? "-fx-background-color:" + purple() + "22;" : "-fx-background-color:transparent;") + "-fx-background-radius:7;");
                    isFirst = false;
                    Label ic = new Label(cmd[0]); ic.setStyle("-fx-font-size:14px; -fx-text-fill:" + purple() + "; -fx-min-width:20;");
                    VBox info = new VBox(2);
                    Label n = new Label(cmd[1]); n.setStyle(fontMain() + " -fx-text-fill:" + textMain() + ";");
                    Label d = new Label(cmd[2]); d.setStyle(fontDim() + " -fx-text-fill:" + textDim() + ";");
                    info.getChildren().addAll(n, d);
                    item.getChildren().addAll(ic, info);
                    item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:" + purple() + "1a; -fx-background-radius:7;"));
                    item.setOnMouseExited(e -> item.setStyle("-fx-background-color:transparent; -fx-background-radius:7;"));
                    String target = cmd[3];
                    item.setOnMouseClicked(e -> {
                        sceneRoot.getChildren().remove(overlay);
                        if ("theme".equals(target)) DARK_MODE.set(!DARK_MODE.get());
                        else if (target != null) { activeSection.set(target); switchSection(target); }
                    });
                    cmdList.getChildren().add(item);
                }
            };
            populateList[0].run();
            search.textProperty().addListener((obs, o, n) -> populateList[0].run());

            palette.getChildren().addAll(pt, search, cmdList);
            overlay.getChildren().add(palette);
            overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) sceneRoot.getChildren().remove(overlay); });
            overlay.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) sceneRoot.getChildren().remove(overlay); });

            sceneRoot.getChildren().add(overlay);
            overlay.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
            ft.setToValue(1); ft.play();
            Platform.runLater(search::requestFocus);
        }


        private HBox sectionHeader(String title, String sub) {
            HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(14,20,14,20));
            header.setStyle("-fx-background-color:" + panelBg() + "; -fx-border-color:" + panelBorder() + "; -fx-border-width:0 0 1 0;");
            Label t = new Label(title); t.setStyle(fontMain() + " -fx-text-fill:" + textMain() + "; -fx-font-weight:bold; -fx-font-size:15px;");
            Label s = new Label(sub); s.setStyle(fontMono() + " -fx-text-fill:" + cyan() + ";");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            header.getChildren().addAll(t, sp, s);
            return header;
        }

        private ScrollPane styledScroll(Node content) {
            ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true);
            sp.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
            return sp;
        }

        private StackPane miniOrb() {
            Circle c = new Circle(12, Color.web(purple() + "33"));
            c.setStroke(Color.web(purple())); c.setStrokeWidth(1.5);
            c.setEffect(new DropShadow(8, Color.web(purple())));
            Arc arc = new Arc(0,0,10,10,0,200); arc.setType(ArcType.OPEN);
            arc.setFill(Color.TRANSPARENT); arc.setStroke(Color.web(cyan())); arc.setStrokeWidth(1.5);
            arc.setStrokeLineCap(StrokeLineCap.ROUND);
            RotateTransition rt = new RotateTransition(Duration.seconds(1.8), arc);
            rt.setByAngle(360); rt.setCycleCount(Animation.INDEFINITE); rt.setInterpolator(Interpolator.LINEAR); rt.play();
            return new StackPane(c, arc);
        }

        private Button neonButton(String text, String color) {
            Button btn = new Button(text);
            btn.setStyle("-fx-background-color:" + color + "1a; -fx-background-radius:20; -fx-border-color:" + color + "88; -fx-border-radius:20; -fx-border-width:1; -fx-text-fill:" + color + "; -fx-font-family:'Courier New'; -fx-font-size:12px; -fx-padding:6 16 6 16; -fx-cursor:hand;");
            btn.setOnMouseEntered(e -> btn.setEffect(new DropShadow(12, Color.web(color))));
            btn.setOnMouseExited(e -> btn.setEffect(null));
            return btn;
        }

        private Button circleBtn(String color) {
            Button btn = new Button(); btn.setPrefSize(12,12); btn.setMinSize(12,12);
            btn.setStyle("-fx-background-color:" + color + "; -fx-background-radius:6; -fx-border-color:#00000033; -fx-border-radius:6; -fx-border-width:0.5; -fx-cursor:hand;");
            return btn;
        }
    }
}