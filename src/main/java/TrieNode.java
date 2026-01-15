
import java.util.HashMap;
import java.util.Map;

public class TrieNode {

	Map<Character, TrieNode> children = new HashMap<>();

	boolean isEndOfWord = false;

	public Map<Character, TrieNode> getChildren(){
		return children;
	}

}
