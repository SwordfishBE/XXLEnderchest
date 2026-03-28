package net.xxlenderchest.config;

/**
 * Represents the configuration for XXL Enderchest.
 *
 * The config file is stored at {@code config/xxlenderchest.json} and can be
 * reloaded at runtime using {@code /xxlenderchest reload} (OP Gamemaster level only).
 */
public class XXLConfig {

    /** Whether the mod is active. When false, the ender chest behaves like vanilla (3 rows). */
    private boolean enabled = true;

    /**
     * When true and LuckPerms is installed, row access is determined by permission nodes.
     * When false, or if LuckPerms is not present, the fixed {@code rows} value is used.
     */
    private boolean useLuckPerms = false;

    /**
     * Number of rows to display in the ender chest GUI.
     * Valid values: 3 (vanilla), 4, 5, 6.
     * Values outside this range are clamped on load.
     */
    private int rows = 6;

    public boolean isEnabled() { return enabled; }
    public boolean isUseLuckPerms() { return useLuckPerms; }

    /** Returns the configured row count, always clamped to [3, 6]. */
    public int getRows() { return rows; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setUseLuckPerms(boolean useLuckPerms) { this.useLuckPerms = useLuckPerms; }
    public void setRows(int rows) { this.rows = rows; }

    /** Clamps rows to the allowed range [3, 6]. */
    public void validate() {
        if (rows < 3) rows = 3;
        if (rows > 6) rows = 6;
    }

    @Override
    public String toString() {
        return "XXLConfig{enabled=" + enabled + ", useLuckPerms=" + useLuckPerms + ", rows=" + rows + "}";
    }
}
