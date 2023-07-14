package com.jballou.shopper.util;


import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.jballou.shopper.ShopperClient;

public final class IO
{
	private IO() {}

	public static boolean save(String name)
	{
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.create();

		String path = String.format(Locale.ROOT, "%s/shops.%s.json", ShopperClient.CONFIG_PATH);
		try 
		{
			FileWriter writer = new FileWriter(path);
		}
		catch (IOException | JsonIOException e)
		{
			// TODO: handle exception
		}
		return false;
	}

	public static boolean load()
	{
		return false;
	}
}
