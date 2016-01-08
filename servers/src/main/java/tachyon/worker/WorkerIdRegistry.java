/*
 * Licensed to the University of California, Berkeley under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package tachyon.worker;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import tachyon.exception.ConnectionFailedException;
import tachyon.master.block.BlockMaster;
import tachyon.thrift.Command;
import tachyon.thrift.CommandType;
import tachyon.thrift.WorkerNetAddress;
import tachyon.worker.block.BlockMasterClient;
import tachyon.worker.block.BlockMasterSync;

/**
 * The single place to get, set, and update worker id.
 *
 * When worker process starts in {@link TachyonWorker}, worker is temporarily lost from master,
 * or when leader master is down in fault tolerant mode, this class will try to get a new worker id
 * from {@link BlockMaster}.
 *
 * Worker id will only be regained in {@link BlockMasterSync} when it receives {@link Command} with
 * type {@link CommandType#Register} from {@link BlockMaster}.
 *
 * This class should be the only source of current worker id within the same worker process.
 */
public final class WorkerIdRegistry {
  /**
   * The default value to initialize worker id, the worker id generated by master will never be the
   * same as this value.
   */
  public static final long INVALID_WORKER_ID = 0;
  private static AtomicLong sWorkerId = new AtomicLong(INVALID_WORKER_ID);

  private WorkerIdRegistry() {}

  /**
   * Registers with {@link BlockMaster} to get a new worker id.
   *
   * @param masterClient the master client to be used for RPC
   * @param workerAddress current worker address
   * @throws IOException when fails to get a new worker id
   * @throws ConnectionFailedException if network connection failed
   */
  public static void registerWithBlockMaster(BlockMasterClient masterClient,
      WorkerNetAddress workerAddress) throws IOException, ConnectionFailedException {
    sWorkerId.set(masterClient.getId(workerAddress));
  }

  /**
   * @return worker id, 0 is invalid worker id, representing that the worker hasn't been registered
   *         with master
   */
  public static Long getWorkerId() {
    return sWorkerId.get();
  }
}
