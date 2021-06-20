package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.Match;

public class JSONFileReader {

	public static List<Match> getMatchOutcomes() {
		List<Match> result = new ArrayList<Match>();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			Match[] matches = mapper.readValue(ResourceLoader.getFile(JSONFileReader.class, "", "results.json"), Match[].class);
			for(Match match : matches) {
				result.add(match);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
