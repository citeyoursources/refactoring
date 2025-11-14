package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */

public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.setInvoice(invoice);
        this.setPlays(plays);
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        final int percentFactor = Constants.PERCENT_FACTOR;
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result;
        result = new StringBuilder("Statement for " + getInvoice().getCustomer() + System.lineSeparator());

        final NumberFormat frmt;
        frmt = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance performance : getInvoice().getPerformances()) {
            final Play play = getPlays().get(performance.getPlayID());

            final int thisAmount = getThisAmount(performance, play);

            // add volume credits
            volumeCredits += Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(play.getType())) {
                volumeCredits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }

            // print line for this order
            final String match = "  %s: %s (%s seats)%n";
            final String name = play.getName();

            result.append(String.format(match, name, frmt.format(thisAmount / percentFactor), performance.getAudience()));
            totalAmount += thisAmount;
        }

        result.append(String.format("Amount owed is %s%n", frmt.format(totalAmount / percentFactor)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private static int getThisAmount(Performance p, Play play) {
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = Constants.TRAGEDY_BASE_AMOUNT;
                if (p.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    final int tragedyOverBaseCapacityPerPerson = Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON;
                    final int tragedyAudienceThreshold = Constants.TRAGEDY_AUDIENCE_THRESHOLD;
                    result += tragedyOverBaseCapacityPerPerson * (p.getAudience() - tragedyAudienceThreshold);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (p.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (p.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * p.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }
}
