package nz.ac.vuw.swen301.tuts.log4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The purpose of this class is to read and merge financial transactions, and print a summary:
 * - total amount
 * - highest/lowest amount
 * - number of transactions
 *
 * <p>Tutorial 6: all console output has been replaced by Log4j 2 logging. Two
 * loggers are used, one per concern:</p>
 *
 * <ul>
 *   <li>{@code FILE} - anything about accessing the transaction files. It is
 *       configured to write to the console <em>and</em> to {@code logs.txt}.</li>
 *   <li>{@code TRANSACTIONS} - the transaction data itself (each imported
 *       purchase and the summary). It is configured to write to the console only.</li>
 * </ul>
 *
 * <p>The level of each call follows the comment that was in the original code,
 * per the table in the tutorial:
 * {@code print info for user -> INFO}, {@code debugging only -> DEBUG},
 * {@code print warning -> WARN}, {@code print error message and details -> ERROR}.</p>
 *
 * @author jens dietrich
 */
public class MergeTransactions {

	private static final Logger FILE_LOG = LogManager.getLogger("FILE");
	private static final Logger TX_LOG = LogManager.getLogger("TRANSACTIONS");

	private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.getDefault());

	public static void main(String[] args) {
		List<Purchase> transactions = new ArrayList<Purchase>();

		// read data from 4 files
		readData("transactions1.csv",transactions);
		readData("transactions2.csv",transactions);
		readData("transactions3.csv",transactions);
		readData("transactions4.csv",transactions);

		// print some info for the user  ->  INFO
		// (this summarises the transaction data, so it belongs to the TRANSACTIONS logger)
		TX_LOG.info("{} transactions imported", transactions.size());
		TX_LOG.info("total value: {}", CURRENCY_FORMAT.format(computeTotalValue(transactions)));
		TX_LOG.info("max value: {}", CURRENCY_FORMAT.format(computeMaxValue(transactions)));

	}

	private static double computeTotalValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = v + p.getAmount();
		}
		return v;
	}

	private static double computeMaxValue(List<Purchase> transactions) {
		double v = 0.0;
		for (Purchase p:transactions) {
			v = Math.max(v,p.getAmount());
		}
		return v;
	}

	// read transactions from a file, and add them to a list
	private static void readData(String fileName, List<Purchase> transactions) {

		File file = new File(fileName);
		String line = null;
		// print info for user  ->  INFO
		FILE_LOG.info("import data from {}", fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine())!=null) {
				String[] values = line.split(",");
				Purchase purchase = new Purchase(
						values[0],
						Double.parseDouble(values[1]),
						DATE_FORMAT.parse(values[2])
				);
				transactions.add(purchase);
				// this is for debugging only  ->  DEBUG
				TX_LOG.debug("imported transaction {}", purchase);
			}
		}
		catch (FileNotFoundException x) {
			// print warning  ->  WARN
			// (passing x as the last argument logs the stack trace with the message)
			FILE_LOG.warn("file {} does not exist - skip", fileName, x);
		}
		catch (IOException x) {
			// print error message and details  ->  ERROR
			FILE_LOG.error("problem reading file {}", fileName, x);
		}
		// happens if date parsing fails
		catch (ParseException x) {
			// print error message and details  ->  ERROR
			FILE_LOG.error("cannot parse date from string - please check whether syntax is correct: {}", line, x);
		}
		// happens if double parsing fails
		catch (NumberFormatException x) {
			// print error message and details  ->  ERROR
			FILE_LOG.error("cannot parse double from string - please check whether syntax is correct: {}", line, x);
		}
		catch (Exception x) {
			// any other exception
			// print error message and details  ->  ERROR
			FILE_LOG.error("exception reading data from file {}, line: {}", fileName, line, x);
		}
		finally {
			try {
				if (reader!=null) {
					reader.close();
				}
			} catch (IOException e) {
				// print error message and details  ->  ERROR
				FILE_LOG.error("cannot close reader used to access {}", fileName, e);
			}
		}
	}

}
