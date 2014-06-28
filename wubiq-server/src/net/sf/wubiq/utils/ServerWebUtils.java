/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Federico Alcantara
 *
 */
public enum ServerWebUtils {
	INSTANCE;
	
	/**
	 * Creates a back response after performing actions that produce a user information.
	 * @param request Originating request.
	 * @param body Contents to be shown in the html body. 
	 * @return A Html string to be used.
	 * @throws IOException
	 */
	public String backResponse(HttpServletRequest request, String body) throws IOException {
		StringBuffer returnValue = new StringBuffer("");
		String context = request.getContextPath().substring(1);
		
		String url =  "/" + context;
		returnValue.append("<html>")
			.append("<header>")
			.append("<meta http-equiv=\"refresh\" content=\"3," + url + "\"/>")
			.append("</header>")
			.append("<body>")
			.append(body)
			.append("</body>")
			.append("</html>");

		return returnValue.toString();
	}

}
