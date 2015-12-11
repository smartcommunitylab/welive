/**
 *    Copyright 2015 Fondazione Bruno Kessler - Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.smartcommunitylab.webtemplate.controllers;

import it.smartcommunitylab.logging.LoggingClient;
import it.smartcommunitylab.logging.model.LogMsg;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.trentorise.smartcampus.aac.AACException;
import eu.trentorise.smartcampus.aac.AACService;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.ProfileServiceException;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;

@Controller
public class ExampleController {
	/**
	 * INSERT A VALID CLIENT_ID AND CLIENT_SECRET CODES FROM SMARTCOMMUNITY
	 * PERMISSION PROVIDER
	 */
	private static final String CLIENT_ID = "";
	private static final String CLIENT_SECRET = "";

	private static final String PROFILE_SERVICE_ENDPOINT = "https://dev.smartcommunitylab.it/aac";
	private static final String AAC_SERVICE_ENDPOINT = "https://dev.smartcommunitylab.it/aac";
	private static final String LOGGING_ENDPOINT = "https://dev.welive.eu/welive.logging";
	private static final String REDIRECT_URI = "http://localhost:8080/welive.quickstart/check";

	private static final String SCOPE = "profile.basicprofile.me";

	private BasicProfileService profileService = new BasicProfileService(
			PROFILE_SERVICE_ENDPOINT);

	private AACService aacService = new AACService(AAC_SERVICE_ENDPOINT,
			CLIENT_ID, CLIENT_SECRET);

	private LoggingClient loggingClient = new LoggingClient(LOGGING_ENDPOINT);
	private String accessToken = "";

	private static final String LOGGING_APPID = "scotest-quickstart";

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public String index(HttpServletRequest request) {

		return "index";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/secure")
	public ModelAndView x(HttpServletRequest request) throws SecurityException,
			ProfileServiceException {
		Map<String, Object> model = new HashMap<String, Object>();
		boolean notAuthorized = false;
		// search token in request headers
		if (accessToken == null) {
			accessToken = analyzeReqHeader(request);
		}
		try {
			if (accessToken != null && accessToken.compareTo("") != 0
					&& aacService.isTokenApplicable(accessToken, SCOPE)) {
				model.put("token", accessToken);
				BasicProfile bp = profileService.getBasicProfile(accessToken);
				model.put("name", bp.getName());
				model.put("surname", bp.getSurname());
			} else {
				notAuthorized = true;
			}
		} catch (AACException e) {
			notAuthorized = true;
		}

		if (notAuthorized) {
			loggingClient
					.log(LOGGING_APPID,
							createLogMessage("Access unauthorized to protected resource"));

			return new ModelAndView("index", "message", "Access unauthorized");

		} else {
			loggingClient.log(LOGGING_APPID, createLogMessage(String.format(
					"User %s request its profile", model.get("surname"))));
			return new ModelAndView("secure", model);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/check")
	public ModelAndView securePage(HttpServletRequest request,
			@RequestParam(required = false) String code)
			throws SecurityException, AACException {

		accessToken = aacService.exchngeCodeForToken(code, REDIRECT_URI)
				.getAccess_token();

		return new ModelAndView("redirect:/secure");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/login")
	public ModelAndView secure(HttpServletRequest request) {
		accessToken = null;

		return new ModelAndView("redirect:"
				+ aacService.generateAuthorizationURIForCodeFlow(REDIRECT_URI,
						null, SCOPE, null));
	}

	private LogMsg createLogMessage(String msg) {
		LogMsg log = new LogMsg();
		log.setMsg(msg);
		log.setType("scoTrack");
		return log;
	}

	private String analyzeReqHeader(HttpServletRequest req) {
		String token = null;
		String httpHeader = req.getHeader("Authorization");
		if (httpHeader != null && httpHeader.contains("Bearer ")) {
			token = httpHeader.substring("Bearer ".length());
		}

		return token;
	}
}
