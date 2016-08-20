package hw1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;// quick import cmd-shift-o
import java.util.Spliterator;
import java.util.function.Consumer;

public class URLlibrary implements Iterable<String> {
	@Override
	public void forEach(Consumer<? super String> action) {
		// TODO Auto-generated method stub
		Iterable.super.forEach(action);
	}

	@Override
	public Spliterator<String> spliterator() {
		// TODO Auto-generated method stub
		return Iterable.super.spliterator();
	}

	private LinkedList<String> urls = new LinkedList<String>();
	
	private int index = 0;
	
	public	URLlibrary(){
		urls.add("http//www.wsj.com");
		urls.add("http//www.kkbox.com");
		urls.add("http//www.bbc.co.uk");
		urls.add("http//www.macbookpro.com");

	}
	@Override
	public boolean hasNext(){
		return index < urls.size();
	}
	@Override
	public String next(){
		try {
			URL url = new URL(urls.get(index));
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while(line = br.readLine() != null){
				sb.append(line);
				sb.append(" \n ");
			}
				br.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	index ++;
	}
	
	
	@Override
	public void remove(){
		urls.remove(index);
	}
	
}

