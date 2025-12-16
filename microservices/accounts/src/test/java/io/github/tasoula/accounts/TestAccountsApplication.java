package io.github.tasoula.accounts;

import org.springframework.boot.SpringApplication;

public class TestAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.from(AccountsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
