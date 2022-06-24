package com.tm.mouctar.encoder;

import javax.smartcardio.*;
import java.nio.charset.StandardCharsets;
import java.time.temporal.Temporal;
import java.util.List;

public class EncodeToCard
{
    private static CardTerminal cardTerminal = null;

    private static Card card = null;

    private static CardChannel cardChannel = null;

    private static int i = 0;

    public static List<CardTerminal> getCardTerminals() throws CardException
    {
        return TerminalFactory.getDefault().terminals().list();
    }

    public static void setCardTerminal(int index) throws CardException
    {
        cardTerminal = getCardTerminals().get(index);
    }

    public static CardTerminal getCardTerminal()
    {
        return cardTerminal;
    }

    public static String readBlock(byte[] blocks) throws CardException
    {
        StringBuilder response = new StringBuilder();
        loadKey();

        for (byte block : blocks) {
            authenticate(block);
            response.append(executeCommand(new CommandAPDU(new byte[]{-1, -80, block, block, 16})));
        }

        return response.toString();
    }

    public static String executeCommand(CommandAPDU commandAPDU) throws CardException
    {
        ResponseAPDU responseAPDU = cardChannel.transmit(commandAPDU);

        if(responseAPDU.getSW() != 36864) throw new CardException("Il y'a un problème.");

        return new String(responseAPDU.getData(), StandardCharsets.ISO_8859_1);
    }

    private static void loadKey() throws CardException
    {
        card = cardTerminal.connect("*");
        cardChannel = card.getBasicChannel();
        executeCommand(new CommandAPDU(new byte[]{-1, -126, 0, 0, 6, -1, -1, -1, -1, -1, -1}));
    }

    private static void authenticate(byte block) throws CardException
    {
        executeCommand(new CommandAPDU(new byte[]{-1, -122, 0, 0, 5, 1, 0, block, 0, 0}));
    }
}
