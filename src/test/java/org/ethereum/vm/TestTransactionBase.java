/**
 * Copyright (c) [2018] [ The Semux Developers ]
 * Copyright (c) [2016] [ <ether.camp> ]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.vm;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.ethereum.vm.client.Block;
import org.ethereum.vm.client.BlockMock;
import org.ethereum.vm.client.Transaction;
import org.ethereum.vm.client.TransactionExecutor;
import org.ethereum.vm.client.TransactionMock;
import org.ethereum.vm.client.TransactionReceipt;
import org.ethereum.vm.client.Unit;
import org.ethereum.vm.util.ByteArrayUtil;
import org.ethereum.vm.util.HashUtil;
import org.ethereum.vm.util.HexUtil;
import org.junit.Before;

public class TestTransactionBase extends TestBase {

    protected final BigInteger premine = BigInteger.valueOf(100L).multiply(Unit.ETH);
    protected final boolean isCreate = false;
    protected final long nonce = 0;

    // by default, it's a CALL transaction with 1 million gas and empty payload
    protected Transaction transaction;
    protected Block block;

    @Before
    public void setup() {
        super.setup();
        transaction = new TransactionMock(isCreate, caller, address, nonce, value, data, gas, gasPrice);
        block = new BlockMock(number, prevHash, coinbase, timestamp, gasLimit);
        repository.addBalance(caller, premine);
    }

    protected byte[] deploy(String code) {
        return deploy(code, EMPTY_BYTE_ARRAY);
    }

    protected byte[] deploy(String code, byte[] arguments) {
        byte[] contractAddress = HashUtil.calcNewAddress(caller, repository.getNonce(caller));

        Transaction tx = spy(transaction);
        when(tx.isCreate()).thenReturn(true);
        when(tx.getTo()).thenReturn(EMPTY_BYTE_ARRAY);
        when(tx.getData()).thenReturn(ByteArrayUtil.merge(HexUtil.fromHexString(code), arguments));

        TransactionExecutor executor = new TransactionExecutor(tx, block, repository, blockStore, false);
        TransactionReceipt receipt = executor.run();

        assertTrue(receipt.isSuccess());
        assertNotNull(repository.getCode(contractAddress));
        assertArrayEquals(receipt.getReturnData(), repository.getCode(contractAddress));

        return contractAddress;
    }
}
