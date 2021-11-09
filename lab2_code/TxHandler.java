//CREATED BY: ERIC HUDDLESTON, HPG103 & JORGE E. FLORES, GCO762

import java.util.*;

public class TxHandler {

	private UTXOPool utxoPool; //'private' variable is made since it needs
	// to be a DEFENSIVE copy. Outer classes cannot see the contents of utxoPool.
	//This helps to maintain privacy of the unspent transactions.

	/* Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is utxoPool. This should make a defensive copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		this.utxoPool = new UTXOPool(utxoPool); //This should make
		//a 'defensive copy' of utxoPool so we declare
		// unspenttxoutPool as a private variable. Using UTXOPool
		//constructor.
	}

	/* Returns true if
	 * (1) all outputs claimed by tx are in the current UTXO pool,
	 * (2) the signatures on each input of tx are valid,
	 * (3) no UTXO is claimed multiple times by tx,
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS

		double totalInput = 0.0; //The total amount of the input.

		double totalOutput = 0.0; //The total amount of the output.

		int txOut = tx.numOutputs(); //The size of the output of transaction

		ArrayList<UTXO> transactionpool = new ArrayList<>(); // here we make a pool of , represented by a resizable array.

		for(int i = 0; i < tx.numInputs(); i++){ //Loop through the entire input of the transaction


			Transaction.Input holderof_input = tx.getInput(i); //Getting the information of all the transactions.
			UTXO utxo = new UTXO(holderof_input.prevTxHash, holderof_input.outputIndex);

			// (1) all outputs claimed by tx are in the current UTXO pool
			if(!utxoPool.contains(utxo)) {
				return false;
			}

			// (2) the signatures on each input of tx are valid,
			Transaction.Output holderof_output = utxoPool.getTxOutput(utxo);

			//Here the dot modifier is used to access the signature (verifySignature) from the public key of the
			//recipient, more specifically in regard to the output. Checking if the signature is within the
			// transaction input (using dot modifier), also checking if the ith input and all outputs of the transaction
			//are valid.
			if(!holderof_output.address.verifySignature(tx.getRawDataToSign(i), holderof_input.signature)){
				return false;
			}

			//(3) no UTXO is claimed multiple times by tx
			if(transactionpool.contains(utxo)){
				return false;
			}
			transactionpool.add(utxo); //Adding the current utxo to the Utxo pool Array list.

			totalInput += holderof_output.value; //The total inputs are set equal to the value of
			//the variable which holds the value of the outputs.
		}

		//(4) : all of tx’s output values are non-negative. Then create outputs as new array list
		for(int i = 0; i < txOut; i++){ //Loop through all the outputs of the transaction.

			Transaction.Output everyOutput = tx.getOutput(i);//every Output will be filled with all the outputs of the
			// transaction in each value of the index. Used the dot modifier
			// to access the Output function from within the Transaction class
			// to create an object which represents all the outputs of the transaction.


			if (everyOutput.value < 0 ){ //None of the transaction output values stored in the everyOutput object can be negative.
				return false;
			} else {
				totalOutput += everyOutput.value; //setting the value of the TOTAL output to
				//the value of every output.
			}
		}

//		(5) the sum of tx’s input values is greater than or equal to the sum of
//		its output values and false otherwise.
		if(totalOutput > totalInput){ //the sum of the transaction's input values cannot be greater than the sum
			// of it's output values, so we can use an if statement to check that.
			return false;
		}
		return true; //if all none of the conditions of clauses 1 - 5 are broken, then true can be returned
	}

	/* Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS

		//Here to represent an unordered array we can simply use a linked list.
		ArrayList<Transaction> acceptedTxs = new ArrayList<>();      //This arrayList will represent a mutually valid array of accepted transactions.

		ArrayList<Transaction> holderofTxs = new ArrayList(Arrays.asList(possibleTxs)); //This resizable array will represent an unordered array of proposed transactions.

		for(int i = 0; i < holderofTxs.size(); i++){ //First we start by looping up to the size of the unordered array
			//of transactions which was received.

			if(isValidTx(holderofTxs.get(i))){  //Checking each transaction for correctness; Call the isValidTx function.

				acceptedTxs.add(holderofTxs.get(i)); //add the array of proposed transactions, which is being
				//iterated through, to the array of valid transactions,
				// if the proposed transactions pass all the checks for correctness.

				for(int j = 0; j < holderofTxs.get(i).getInputs().size(); j++){  //loop through the inputs of the proposed transaction, up until
					//the size of the inputs of the proposed transaction. getInputs() is a resizable array.

					//Here I am iterating through the proposed transactions --> size of the proposed transactions (outer)
					// --> size of proposed transactions (inner) --> hash of the previous transaction, output Index.
					UTXO tempUtxo = new UTXO(holderofTxs.get(i).getInput(j).prevTxHash, holderofTxs.get(i).getInput(j).outputIndex);

					//Remove the old inputs; using removeUTXO(), which Removes the UTXO <utxo> from the pool
					utxoPool.removeUTXO(tempUtxo);
				}
				for(int j = 0; j < holderofTxs.get(i).getOutputs().size(); j++){ //Loop through the outputs of the proposed transaction, up until
					//the size of the outputs of the proposed transaction. getOutputs() is a resizable array.

					UTXO tempUtxo = new UTXO(holderofTxs.get(i).getHash(), j); //Using the hash corresponding to
					//the output (UTXO.java).
					utxoPool.addUTXO(tempUtxo, holderofTxs.get(i).getOutput(j));
				}
			}
		}
		//Creating a new object of which extends Transaction, adding values to mutually valid
		//array of excepted transactions.
		Transaction[] acceptedTxsFinal = new Transaction[acceptedTxs.size()];

		for(int finalval = 0; finalval < acceptedTxs.size(); finalval++){ //Looping through the size of the excepted transactions.

			acceptedTxsFinal[finalval] = acceptedTxs.get(finalval); //Setting the values of the mutually valid array of excepted transactions.
		}

		return acceptedTxsFinal; //Returning the mutually valid array of excepted transactions.
	}

}
