package net.sf.wubiq.utils;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Localized server labels.
 */
public class ServerLabelsTest {
	private Locale storedDefault;

	@Before
	public void storeDefaultLocale() {
		storedDefault = Locale.getDefault();
	}

	@After
	public void restoreLocale() {
		ServerLabels.setLocale(Locale.ENGLISH);
		Locale.setDefault(storedDefault);
	}

	@Test
	public void clientsByVersionInEnglish() {
		ServerLabels.setLocale(Locale.ENGLISH);
		assertEquals("Connected clients by version: 2.7.4 (3)", ServerLabels.get("server.clients_by_version", "2.7.4 (3)"));
	}

	@Test
	public void clientsByVersionInSpanish() {
		ServerLabels.setLocale(new Locale("es"));
		assertEquals("Clientes conectados por versión: 2.7.4 (3)", ServerLabels.get("server.clients_by_version", "2.7.4 (3)"));
	}

	@Test
	public void missingLabelReturnsItsKey() {
		ServerLabels.setLocale(Locale.ENGLISH);
		assertEquals("server.no_such_label", ServerLabels.get("server.no_such_label"));
	}
}
