package squeek.appleskin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import squeek.appleskin.network.SyncHandler;

public class AppleSkin implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		SyncHandler.init();
	}
}
