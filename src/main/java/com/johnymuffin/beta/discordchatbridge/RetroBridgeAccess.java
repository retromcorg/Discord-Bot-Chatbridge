package com.johnymuffin.beta.discordchatbridge;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.retromc.retrobridge.RetroBridge;
import org.retromc.retrobridge.bridge.BridgeManager;
import org.retromc.retrobridge.bridge.afk.AFKBridge;
import org.retromc.retrobridge.bridge.auth.AuthBridge;
import org.retromc.retrobridge.bridge.economy.EconomyBridge;
import org.retromc.retrobridge.bridge.fakequit.FakeQuitBridge;
import org.retromc.retrobridge.bridge.permission.PermissionBridge;
import org.retromc.retrobridge.bridge.vanish.VanishBridge;
import org.retromc.retrobridge.bridge.whois.WhoisBridge;

/**
 * Small consumer-facing helper for plugins that want to talk to RetroBridge.
 *
 * Typical usage:
 * RetroBridgeAccess bridge = new RetroBridgeAccess();
 * if (bridge.isAvailable()) {
 *     EconomyBridge economy = bridge.getEconomyBridge();
 * }
 */
public final class RetroBridgeAccess {
    public static final String PLUGIN_NAME = "RetroBridge";

    public boolean isAvailable() {
        return getRetroBridge() != null;
    }

    public RetroBridge getRetroBridge() {
        RetroBridge instance = RetroBridge.getInstance();
        if (instance != null) {
            return instance;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin instanceof RetroBridge && plugin.isEnabled()) {
            return (RetroBridge) plugin;
        }
        return null;
    }

    public BridgeManager getBridgeManager() {
        RetroBridge retroBridge = getRetroBridge();
        if (retroBridge == null) {
            return null;
        }
        return retroBridge.getBridgeManager();
    }

    public AFKBridge getAFKBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getAFKBridge();
    }

    public EconomyBridge getEconomyBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getEconomyBridge();
    }

    public PermissionBridge getPermissionBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getPermissionBridge();
    }

    public AuthBridge getAuthBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getAuthBridge();
    }

    public WhoisBridge getWhoisBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getWhoisBridge();
    }

    public VanishBridge getVanishBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getVanishBridge();
    }

    public FakeQuitBridge getFakeQuitBridge() {
        BridgeManager manager = getBridgeManager();
        if (manager == null) {
            return null;
        }
        return manager.getFakeQuitBridge();
    }
}