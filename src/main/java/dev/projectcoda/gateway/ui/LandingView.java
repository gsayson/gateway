package dev.projectcoda.gateway.ui;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * The landing view. Regular users can only access this.
 * @author Gerard Sayson
 */
@Route("/")
@AnonymousAllowed
public class LandingView extends VerticalLayout {

	public LandingView() {
		add(
				new H1("Coda Gateway"),
				new Label("Gateway for restricted services in Project Coda."),
				new Span(new Label("Play on "), new Anchor("https://projectcoda.dev"), new Label(", a new speed-oriented competitive programming game."))
		);
	}

}
