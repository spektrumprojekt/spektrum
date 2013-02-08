package de.spektrumprojekt.commons.chain;

/**
 * The Proxy Command can be used to reuse chains within a command fulfilling generic constraint
 * 
 * @author tlu
 * 
 * @param <OC>
 *            The type of the context the (output) command to be executed will have
 * @param <IC>
 *            The type of context the the (input) context will have
 */

public class ProxyCommand<OC, IC extends OC> implements Command<IC> {

    private Command<OC> chain;

    public ProxyCommand(Command<OC> chain) {
        this.chain = chain;
    }

    @Override
    public String getConfigurationDescription() {
        return chain.getConfigurationDescription();
    }

    @Override
    public void process(IC context) {
        chain.process(context);
    }

}