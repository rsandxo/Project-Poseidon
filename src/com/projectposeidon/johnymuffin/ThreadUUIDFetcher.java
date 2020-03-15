package com.projectposeidon.johnymuffin;

import com.projectposeidon.PoseidonConfig;
import net.minecraft.server.Packet1Login;
import org.bukkit.ChatColor;

import java.util.UUID;

import static com.projectposeidon.evilmidget38.UUIDFetcher.getUUIDOf;
import static com.projectposeidon.johnymuffin.UUIDPlayerStorage.generateOfflineUUID;

public class ThreadUUIDFetcher extends Thread {

    final Packet1Login loginPacket;
    //    final NetLoginHandler netLoginHandler;
    final LoginProcessHandler loginProcessHandler;

    public ThreadUUIDFetcher(Packet1Login packet1Login, LoginProcessHandler loginProcessHandler) {
//        this.netLoginHandler = netloginhandler; // The login handler
        this.loginProcessHandler = loginProcessHandler;
        this.loginPacket = packet1Login; // The login packet

    }

    public void run() {
        UUID uuid = UUIDPlayerStorage.getInstance().getPlayerUUID(loginPacket.name);
        if (uuid == null) {
            try {
                uuid = getUUIDOf(loginPacket.name);
                if (uuid == null) {
                    if (PoseidonConfig.getInstance().isAllowGracefulUUIDEnabled()) {
                        System.out.println(loginPacket.name + " does not have a Mojang UUID associated with their name");
                        UUID offlineUUID = generateOfflineUUID(loginPacket.name);
                        System.out.println("Using Offline Based UUID for " + loginPacket.name + " - " + offlineUUID);
                        UUIDCacheFile.getInstance().addPlayerDetails(loginPacket.name, offlineUUID, false);
                        loginProcessHandler.userUUIDReceived();
                    } else {
                        System.out.println(loginPacket.name + " does not have a UUID with Mojang. Player has been kicked as graceful UUID is disabled");
                        loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we only support premium accounts");
                    }

                } else {
                    System.out.println("Fetched UUID from Mojang for " + loginPacket.name + " - " + uuid.toString());
                    try {
                        UUIDPlayerStorage.getInstance().addPlayerOnlineUUID(loginPacket.name, uuid);
                        UUIDCacheFile.getInstance().addPlayerDetails(loginPacket.name, uuid, true);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    loginProcessHandler.userUUIDReceived();
                }
                //netLoginHandler.authenticatePlayer(loginPacket);
                //netLoginHandler.playerUUIDFetched(loginPacket);


            } catch (Exception e) {
                //this.netLoginHandler.disconnect(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");
                System.out.println("Mojang failed contact for user " + loginPacket.name + ": " + e.getMessage());
                loginProcessHandler.cancelLoginProcess(ChatColor.RED + "Sorry, we can't connect to Mojang currently, please try again later");
            }
        } else {
            System.out.println("Fetched UUID from Cache for " + loginPacket.name + " - " + uuid.toString());
            //netLoginHandler.authenticatePlayer(loginPacket);
            loginProcessHandler.userUUIDReceived();
        }


    }
}


