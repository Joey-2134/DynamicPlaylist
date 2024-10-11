package com.dynamicPlaylists.dynamicPlaylists;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication()
public class DynamicPlaylistsApplication {

	private final Environment env;

	public DynamicPlaylistsApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		SpringApplication.run(DynamicPlaylistsApplication.class, args);
	}

}
