package squeek.appleskin.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import squeek.appleskin.helpers.HungerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SyncHandler
{
	private static final Identifier EXHAUSTION_SYNC = new Identifier("appleskin", "exhaustion_sync");
	private static final Identifier SATURATION_SYNC = new Identifier("appleskin", "saturation_sync");

	public static void init()
	{
		CustomPayloadPacketRegistry.CLIENT.register(EXHAUSTION_SYNC, (packetContext, packetByteBuf) ->
		{
			MinecraftClient.getInstance().execute(() -> {
			  	HungerHelper.setExhaustion(packetContext.getPlayer(), packetByteBuf.readFloat());
			});
		});
		CustomPayloadPacketRegistry.CLIENT.register(SATURATION_SYNC, (packetContext, packetByteBuf) ->
		{
			MinecraftClient.getInstance().execute(() -> {
			  	packetContext.getPlayer().getHungerManager().setSaturationLevelClient(packetByteBuf.readFloat());
		  	});
		});
	}

	private static CustomPayloadClientPacket makeSyncPacket(Identifier identifier, float val)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeFloat(val);
		return new CustomPayloadClientPacket(identifier, buf);
	}

	/*
	 * Sync saturation (vanilla MC only syncs when it hits 0)
	 * Sync exhaustion (vanilla MC does not sync it at all)
	 */
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<UUID, Float>();
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<UUID, Float>();

	public static void onPlayerUpdate(ServerPlayerEntity player)
	{
		Float lastSaturationLevel = lastSaturationLevels.get(player.getUuid());
		Float lastExhaustionLevel = lastExhaustionLevels.get(player.getUuid());

		float saturation = player.getHungerManager().getSaturationLevel();
		if (lastSaturationLevel == null || lastSaturationLevel != saturation)
		{
			player.networkHandler.sendPacket(makeSyncPacket(SATURATION_SYNC, saturation));
			lastSaturationLevels.put(player.getUuid(), saturation);
		}

		float exhaustionLevel = HungerHelper.getExhaustion(player);
		if (lastExhaustionLevel == null || Math.abs(lastExhaustionLevel - exhaustionLevel) >= 0.01f)
		{
			player.networkHandler.sendPacket(makeSyncPacket(EXHAUSTION_SYNC, exhaustionLevel));
			lastExhaustionLevels.put(player.getUuid(), exhaustionLevel);
		}
	}

	public static void onPlayerLoggedIn(ServerPlayerEntity player)
	{
		lastSaturationLevels.remove(player.getUuid());
		lastExhaustionLevels.remove(player.getUuid());
	}
}
