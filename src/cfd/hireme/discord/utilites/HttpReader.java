package cfd.hireme.discord.utilites;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import cfd.hireme.discord.commands.data.StoreItem;

public class HttpReader {
	String httpUrl;
	StoreType storeType;
	boolean api;
	boolean isNewReader;
	JsonObject jsonObject;
	List<StoreItem> storeItems = new ArrayList<StoreItem>();

	public static enum StoreType {
		EPIC, UBISOFT, STEAM;
	}

	public HttpReader(String url, StoreType type, boolean isApi, boolean isNew)
			throws IOException, InterruptedException {
		this.httpUrl = url;
		this.storeType = type;
		this.api = isApi;
		this.isNewReader = isNew;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(this.httpUrl));
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).executor(executor).build();

		if (isApi) {
			HttpResponse<String> response = client.send(request.build(), BodyHandlers.ofString());
			this.jsonObject = this.parseJson(response.body());
			try {
				this.parseStoreItemToList(this.jsonObject, type);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			response = null;
		} else {
			if (type == StoreType.UBISOFT) {
				HttpResponse<Stream<String>> response = client.send(request.build(), BodyHandlers.ofLines());
				Stream<String> filtered = response.body().filter(new Predicate<String>() {

					@Override
					public boolean test(String t) {
						// TODO Auto-generated method stub
						return t.contains("var product = {");
					}

				});
				Gson gson = new Gson();
				filtered.forEach((String string) -> {

					string = string.substring(string.indexOf("var product = ") + "var product = ".length());
					StringReader stringReader = new StringReader(string);
					JsonReader reader = new JsonReader(stringReader);
					reader.setLenient(true);
					JsonObject json = gson.fromJson(reader, JsonObject.class);
					if (json.has("id")) {
						this.parseStoreItemToList(json, StoreType.UBISOFT);
					}
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					stringReader.close();
				});
				filtered.close();
				filtered = null;
				response = null;
			}
		}
		executor.shutdownNow();
		client = null;
		request = null;
	}

	public List<StoreItem> getStoreItems() {
		return this.storeItems;
	}

	public JsonObject getJson() {
		return this.jsonObject;
	}

	private void parseStoreItemToList(JsonObject object, StoreType type) {
		switch (type) {
		case EPIC:
			for (String string : Arrays.asList("data", "Catalog", "searchStore")) {
				object = object.get(string).getAsJsonObject();
			}
			JsonArray games = object.get("elements").getAsJsonArray();
			for (Object gameObject : games) {
				if (gameObject instanceof JsonObject) {
					JsonObject priceObject = ((JsonObject) ((JsonObject) ((JsonObject) gameObject).get("price"))
							.get("totalPrice"));

					String title = ((JsonObject) gameObject).get("title").getAsString();
					System.out.println("Grabbing information: \"" + title + "\"");
					long price = priceObject.get("originalPrice").getAsLong();
					long discount = priceObject.get("discountPrice").getAsLong();
					String currency = priceObject.get("currencyCode").getAsString();
					long decimalPlaces = priceObject.get("currencyInfo").getAsJsonObject().get("decimals").getAsLong();
					String imageDisplay = null;
					boolean isBundled = false;
					for (JsonElement element : ((JsonObject) gameObject).get("categories").getAsJsonArray()) {
						if (element instanceof JsonObject) {
							if (element.getAsJsonObject().has("path")) {
								if (element.getAsJsonObject().get("path").getAsString().equals("bundles")) {
									isBundled = true;
								}
							}
						}
					}
					String seller = ((JsonObject) gameObject).get("seller").getAsJsonObject().get("name").getAsString();

					for (Object imageObject : ((JsonObject) gameObject).get("keyImages").getAsJsonArray()) {
						if (imageObject instanceof JsonObject) {
							if (((JsonObject) imageObject).get("type").getAsString().equals("Thumbnail")) {
								imageDisplay = ((JsonObject) imageObject).get("url").getAsString();
							}
						}
					}
					if (imageDisplay == null) {
						imageDisplay = ((JsonObject) gameObject).get("keyImages").getAsJsonArray().get(0)
								.getAsJsonObject().get("url").getAsString();
					}
					String storePage = ((JsonObject) gameObject).get("productSlug").getAsString();
					if (storePage != null) {
						storePage = String.format("https://www.epicgames.com/store/en-US/%s/%s",
								isBundled ? "bundles" : "product", storePage);

						String startDate = ((JsonObject) gameObject).get("effectiveDate").getAsString();
						if (LocalDate.parse(startDate, StoreItem.DATE_FORMATTER).isBefore(LocalDate.now())) {
							if (!((JsonObject) gameObject).get("promotions").isJsonNull()) {
								if (!((JsonObject) gameObject).get("promotions").getAsJsonObject()
										.get("promotionalOffers").isJsonNull()) {
									if (((JsonObject) gameObject).get("promotions").getAsJsonObject()
											.get("promotionalOffers").getAsJsonArray().size() > 0) {
										if (((JsonObject) gameObject).get("promotions").getAsJsonObject()
												.get("promotionalOffers").getAsJsonArray().get(0).getAsJsonObject()
												.get("promotionalOffers").getAsJsonArray().size() > 0) {
											startDate = ((JsonObject) gameObject).get("promotions").getAsJsonObject()
													.get("promotionalOffers").getAsJsonArray().get(0).getAsJsonObject()
													.get("promotionalOffers").getAsJsonArray().get(0).getAsJsonObject()
													.get("startDate").getAsString();
										}
									}
								}
							}
						}
						StoreItem item = new StoreItem(StoreType.EPIC, title, seller, price, discount, currency,
								decimalPlaces, storePage, imageDisplay, startDate);
						if (!item.getEndDateObject().isBefore(LocalDate.now())
								&& !(item.getEndDateObject().getYear() == 2099)) {
							System.out.println(
									item.getTitle() + ": " + item.getOriginalPrice() + "/" + item.getDiscountPrice());
							// TEST : 0 to Free
							if (item.getDiscountPrice().equals("Free") ? true
									: !(item.getOriginalPrice().equals(item.getDiscountPrice()))) {
								this.storeItems.add(item);
							} else {
								item = null;
							}
						} else {
							item = null;
						}
					} else {
						System.out.println("productSlug for: \"" + title + "\" error, examine page");
					}
				}
			}
			break;
		case UBISOFT:
			if (object.get("unit_price").getAsLong() != object.get("unit_sale_price").getAsLong()
					|| object.get("unit_price").getAsLong() == 0) {
				String storeUrl = object.get("url").getAsString();
				String brand = object.get("brand").getAsString();
				String name = object.get("name").getAsString().toLowerCase();
				System.out.println("Grabbing information: \"" + name + "\"");
				if (name.contains("pc-dig-")) {
					name = name.replace("pc-dig-", "");
				}
				if (name.contains("-ww")) {
					name = name.replace("-ww", "");
				}
				String splitName[] = name.split(" ");
				name = "";
				for (String string : splitName) {
					name = name + string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase() + " ";
				}
				String imageUrl = object.get("image_url").getAsString();
				String currency = object.get("currency").getAsString();
				long price = object.get("unit_price").getAsLong();
				long discount = object.get("unit_sale_price").getAsLong();
				this.storeItems.add(new StoreItem(StoreType.UBISOFT, name, brand, price, discount, currency, null,
						storeUrl, imageUrl, null));
			}
			break;
		case STEAM:
			for (JsonElement unitElement : object.get("featured_win").getAsJsonArray()) {
				if (unitElement instanceof JsonObject) {
					if (((JsonObject) unitElement).get("final_price").getAsLong() == 0) {
						JsonObject obj = (JsonObject) unitElement;
						System.out.println("Grabbing information: \"" + obj.get("name").getAsString() + "\"");
						String storeUrl = "https://store.steampowered.com/app/" + obj.get("id").getAsLong() + "/"
								+ obj.get("name").getAsString().replace(" ", "_") + "/";
						long price = obj.get("discounted").getAsBoolean() ? obj.get("original_price").getAsLong() : 0L;
						String availability = "";
						if (obj.get("windows_available").getAsBoolean()) {
							availability = availability + "Windows\n";
						}
						if (obj.get("mac_available").getAsBoolean()) {
							availability = availability + "Macintosh\n";
						}
						if (obj.get("linux_available").getAsBoolean()) {
							availability = availability + "Linux\n";
						}
						Long decimalPlaces = null;
						if (price != 0) {
							String strPrice = Long.toString(price);
							if (strPrice.length() >= 4) {
								decimalPlaces = (long) (strPrice.length() - 2);
							}
						}
						this.storeItems.add(new StoreItem(StoreType.STEAM, obj.get("name").getAsString(), availability,
								price, obj.get("final_price").getAsLong(), obj.get("currency").getAsString(),
								decimalPlaces, storeUrl, obj.get("large_capsule_image").getAsString(), null));
					}
				}
			}
			break;
		default:
			break;

		}

	}

	private JsonObject parseJson(String string) {
		JsonObject json = new Gson().fromJson(string, JsonObject.class);
		return json;
	}

}
