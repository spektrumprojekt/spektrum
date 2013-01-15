package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.informationextraction.extractors.KeyphraseCandidates;
import de.spektrumprojekt.informationextraction.extractors.KeyphraseExtractorCommand;
import de.spektrumprojekt.informationextraction.extractors.LanguageDetectorCommand;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

@RunWith(Parameterized.class)
public class KeyphraseExtractorTest {

    private final String title;
    private final String text;
    private final String language;

    public KeyphraseExtractorTest(String title, String text, String language) {
        this.title = title;
        this.text = text;
        this.language = language;
    }

    @Parameters
    public static Collection<Object[]> testData() {
        String text1 = "Romney launching new attack on Obama over welfare law";
        Object[] msg1 = new Object[] {null, text1, "en"};
        String text2 = "#London2012 #Olympics: Tuesday 7 August - Live picture blog";
        Object[] msg2 = new Object[] {null, text2, "en"};
        String text3 = "Arsenal confirm signing of Spain international Santi Cazorla http://gu.com/p/39hfg/tw via @guardian_sport";
        Object[] msg3 = new Object[] {null, text3, "en"};
        String title4 = "The surprising, stealth rebirth of the American arcade";
        String text4 = "We might be living in the era of the 99 cent app, but there's stilly plenty of magic to be had for a quarter. Aurich Lawson The arcade industry is dead in the United States—everyone knows it—done in by a combination of rapidly advancing home consoles and rapidly expanding suburbanization in the late '80s and early '90s. The only people not in on this bit of conventional wisdom are the ones who happen to be opening a surprising number of successful new arcades around the country.Adam Pratt, who runs industry website Arcade Heroes when he isn't managing his own arcade in West Valley City, Utah, tracked at least 12 major, dedicated, independent US arcades opening their doors in 2011, with 10 more opening so far this year. That might not be enough to rival numbers from the golden age of arcades, but it's a notable expansion from the years before. \"I have missed plenty of locations, but despite that, there really has been an increase over the past two years or so,\" Pratt told me. \"News occasionally comes along of a place closing, but it is far outweighed by openings.\" And almost all of these locations are thriving, based on what Pratt has been hearing.";
        Object[] msg4 = new Object[] {title4, text4, "en"};
        String title5 = "Filling in the gaps: great enterprise focused add-ons for OS X Server";
        String text5 = "In our review of OS X Server, we found that Mountain Lion has a lot to offer home users or Mac-centric small businesses. Enterprise-level features, however, have fallen by the wayside. Luckily, some great first- and third-party tools exist to help close the gap between Apple's server product and more robust enterprise management systems from the likes of Microsoft and Dell.Some of the products are free open-source programs, and some are strong, for-pay products intended for use with hundreds if not thousands of Macs. Whatever your needs are, this list of applications should point you in the right direction if you're looking to extend OS X Server's capabilities.Apple Remote DesktopSending a Software Update UNIX command with Apple Remote Desktop. One of OS X Server's most glaring blind spots relative to Windows Server and Active Directory is software management. There's no way to install third-party applications on Macs that are already out in the field. And if you use a program like DeployStudio to install applications when you set up your Mac, it isn't much help to you once the Mac is off your desk and out in the field.";
        Object[] msg5 = new Object[] {title5, text5, "en"};
        return Arrays.asList(msg1, msg2, msg3, msg4, msg5);
    }

    @Test
    public void testKeyphraseExtractor() {
        KeyphraseExtractorCommand keyphraseExtractor = new KeyphraseExtractorCommand();
        Message message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
        message.addProperty(new Property(Property.PROPERTY_KEY_TITLE, title));
        message.addProperty(new Property(LanguageDetectorCommand.LANGUAGE, language));
        
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, text);
        InformationExtractionContext context = new InformationExtractionContext(new SimplePersistence(), message, messagePart);
        context.setCleanText(text);
        keyphraseExtractor.process(context);
        
        System.out.println(messagePart.getScoredTerms());
    }

    @Test
    public void testRemoveOverlap() {
        KeyphraseCandidates candidates = new KeyphraseCandidates();
        candidates.addCandidate("os x server", "os x server", 3);
        candidates.addCandidate("x server", "os x server", 3);
        candidates.addCandidate("server", "server", 5);
        candidates.removeOverlaps();

        assertEquals(3, candidates.getCandidate("os x server").getCount());
        assertEquals(0, candidates.getCandidate("x server").getCount());
        assertEquals(2, candidates.getCandidate("server").getCount());
    }

}
