package org.infinispan.interceptors.impl;

import org.infinispan.commands.DataCommand;
import org.infinispan.commands.write.DataWriteCommand;
import org.infinispan.commands.write.WriteCommand;
import org.infinispan.context.InvocationContext;
import org.infinispan.context.impl.FlagBitSets;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.interceptors.InvocationFinallyFunction;
import org.infinispan.scattered.BiasManager;

public class BiasedEntryWrappingInterceptor extends RetryingEntryWrappingInterceptor {
   private static final long NOT_BIASING_FLAGS = FlagBitSets.PUT_FOR_STATE_TRANSFER |
         FlagBitSets.PUT_FOR_X_SITE_STATE_TRANSFER | FlagBitSets.CACHE_MODE_LOCAL;

   private BiasManager biasManager;
   private final InvocationFinallyFunction<DataWriteCommand> handleDataWriteReturn = this::handleDataWriteReturn;
   private final InvocationFinallyFunction<WriteCommand> handleManyWriteReturn = this::handleManyWriteReturn;

   @Inject
   public void inject(BiasManager biasManager) {
      this.biasManager = biasManager;
   }

   @Override
   protected boolean canRead(DataCommand command) {
      return biasManager.hasLocalBias(command.getKey()) || super.canRead(command);
   }

   @Override
   protected boolean canReadKey(Object key) {
      return biasManager.hasLocalBias(key) || super.canReadKey(key);
   }

   @Override
   protected Object setSkipRemoteGetsAndInvokeNextForDataCommand(InvocationContext ctx, DataWriteCommand command) {
      return invokeNextAndHandle(ctx, command, handleDataWriteReturn);
   }

   @Override
   protected Object setSkipRemoteGetsAndInvokeNextForManyEntriesCommand(InvocationContext ctx, WriteCommand command) {
      return invokeNextAndHandle(ctx, command, handleManyWriteReturn);
   }

   private Object handleDataWriteReturn(InvocationContext ctx, DataWriteCommand dataWriteCommand, Object rv, Throwable throwable) throws Throwable {
      if (throwable != null) {
         return super.handleDataWriteReturn(ctx, dataWriteCommand, throwable);
      } else if (dataWriteCommand.isSuccessful() && ctx.isOriginLocal()) {
         if (dataWriteCommand.hasAnyFlag(NOT_BIASING_FLAGS)) {
            return rv;
         }
         if (!distributionManager.getCacheTopology().getDistribution(dataWriteCommand.getKey()).isPrimary()) {
            biasManager.addLocalBias(dataWriteCommand.getKey(), dataWriteCommand.getTopologyId());
         }
      }
      return rv;
   }

   private Object handleManyWriteReturn(InvocationContext ctx, WriteCommand writeCommand, Object rv, Throwable throwable) throws Throwable {
      if (throwable != null) {
         return super.handleManyWriteReturn(ctx, writeCommand, throwable);
      } else if (writeCommand.isSuccessful() && ctx.isOriginLocal()) {
         if (writeCommand.hasAnyFlag(NOT_BIASING_FLAGS)) {
            return rv;
         }
         for (Object key : writeCommand.getAffectedKeys()) {
            if (!distributionManager.getCacheTopology().getDistribution(key).isPrimary()) {
               biasManager.addLocalBias(key, writeCommand.getTopologyId());
            }
         }
      }
      return rv;
   }
}
