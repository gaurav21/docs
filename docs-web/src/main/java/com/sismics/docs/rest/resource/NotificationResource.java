/**
 * 
 */
package com.sismics.docs.rest.resource;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.sismics.rest.exception.ForbiddenClientException;

/**
 * @author gaurav
 *
 */
@Path("/notification")
public class NotificationResource extends BaseResource {

	@GET
	public Response recieveNotification() {
//		if (!authenticate()) {
//            throw new ForbiddenClientException();
//        }
		
		// Create your bot passing the token received from @BotFather
		TelegramBot bot = new TelegramBot("BOT_TOKEN");

		// Register for updates
		bot.setUpdatesListener(updates -> {
		    // ... process updates
		    // return id of last processed update or confirm them all
		    return UpdatesListener.CONFIRMED_UPDATES_ALL;
		});

		// Send messages
//		long chatId = update.message().chat().id();
//		SendResponse sresponse = bot.execute(new SendMessage(chatId, "Hello!"));
		
		// Returns the vocabulary
        JsonObjectBuilder response = Json.createObjectBuilder();
        return Response.ok().entity(response.build()).build();
	}
}
