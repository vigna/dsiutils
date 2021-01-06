/** Main classes manipulating bits
 *
 * <p>The {@link it.unimi.dsi.bits.BitVector} interface is the basis for bit vector manipulation.
 * The {@link it.unimi.dsi.bits.LongArrayBitVector} implementation is its main implementation. 
 * The idea is to offer an efficent but easy-to-use bit-vector class by allowing access under many different <em>views</em>. For instance,
 * a bit vector can be seen as a {@link it.unimi.dsi.fastutil.longs.LongBigList} of integers of fixed width. Or as a sorted set of
 * integers, where the positions of the bits set to one represent elements.
 * 
 * <p>Whenever another object has to be turned into a bit string, you can provide a
 * {@link it.unimi.dsi.bits.TransformationStrategy} to that purpose. The static container
 * {@link it.unimi.dsi.bits.TransformationStrategies} has several ready-made transformations,
 * and some useful wrapping methods.
 */

package it.unimi.dsi.bits;
