import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Trie {

	TrieNode root;

    public Trie() {
      this.root = new TrieNode();
    }
    
    public void insert(String word) {
        TrieNode node = root;
        for(char character : word.toCharArray()){
            node.children.putIfAbsent(character, new TrieNode());
            node = node.children.get(character);
        }

        node.isEndOfWord = true;

    }

	public void insertWords(Set<String> words){
		for(String word : words){
			insert(word);
		}
	}
    
    public boolean search(String word) {

        TrieNode node = root;

        for(char character : word.toCharArray()){

            if(!node.children.containsKey(character))
                return false;

            node = node.children.get(character);
        }

        return node.isEndOfWord;
        
    }
    
    public boolean startsWith(String prefix) {

        TrieNode node = root;

        for(char character : prefix.toCharArray()){

            if(!node.children.containsKey(character))
                return false;

            node = node.children.get(character);

        }

        return true;
        
    }

    public List<String> autocomplete(String prefix) {
        TrieNode node = root;

        //find the node corresponding to the prefix
        for(Character character : prefix.toCharArray()){
            if(!node.children.containsKey(character)){
                return Collections.emptyList();
            }

            node = node.children.get(character);
        }

        // Collect all matching words
        List<String> results = new ArrayList<>();
        collect(node, results, prefix);
        return results;
    }

    public void collect(TrieNode node, List<String> results, String words){

            if(node.isEndOfWord){
                results.add(words);
            }

            for(var entry : node.children.entrySet()){
                char nextChar = entry.getKey();
                TrieNode nextNode = entry.getValue();

                collect(nextNode, results, words + nextChar);

            }

    }

	public TrieNode getNode(){
		return root;
	}

}
