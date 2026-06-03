package de.btu.kt.syx.devices.tg55;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import de.btu.kt.syx.SYX;
import de.btu.kt.syx.devices.BankTree;

/**
 * A patch bank.
 * 
 * <p>
 * TODO: Replace by or derive from {@link BankTree}!
 * </p>
 * 
 * @author Matthias Wolff
 */
public class TG55PatchBank implements Serializable
{

  private static final long serialVersionUID = 1L;

  // -- Attributes ------------------------------------------------------------

  /**
   * The bank IDs.
   */
  protected int[] bankIDs;

  /**
   * The bank names.
   */
  protected HashMap<Integer,String> bankNames;
  
  /**
   * The patch names.
   */
  protected HashMap<Integer,ArrayList<String>> patchNames;

  /**
   * Format string of patch IDs.
   * 
   * @see #getPatchID(int, int)
   */
  protected String fPatchID;
  
  // -- Constructors ----------------------------------------------------------

  /**
   * Creates a new patch bank.
   * 
   * @param bankIDs
   *          Array of patch bank IDs
   * @param bankNames
   *          Array of patch bank names
   * @param memSizes
   *          Array of patch counts for each patch bank
   * @throws IllegalArgumentException
   *          if either argument is {@code null}, or if the arguments have a
   *          different number of elements
   */
  public TG55PatchBank(int[] bankIDs, String[] bankNames, int[] memSizes)
  throws IllegalArgumentException
  {
    // Sanity checks
    if (bankIDs==null)
      throw SYX.IllArgExc("Invalid 'bankIDs' must not be null");
    if (bankNames==null)
      throw SYX.IllArgExc("Invalid 'bankNames' must not be null");
    if (memSizes==null)
      throw SYX.IllArgExc("Invalid 'memSizes' must not be null");
    if (bankIDs.length != memSizes.length)
      throw SYX.IllArgExc("Arguments 'bankIDs' and 'memSizes' do not match");
    if (bankIDs.length != bankNames.length)
      throw SYX.IllArgExc("Arguments 'bankIDs' and 'bankNames' do not match");

    // Initialize arrays
    this.bankIDs = bankIDs;
    this.patchNames = new HashMap<Integer,ArrayList<String>>();
    this.bankNames = new HashMap<Integer,String>();
    for (int i=0; i<bankIDs.length; i++)
    {
      this.bankNames.put(bankIDs[i],bankNames[i]);
      this.patchNames.put(bankIDs[i],new ArrayList<String>());
      for (int j=0; j<memSizes[i]; j++)
        this.patchNames.get(bankIDs[i]).add("???");
    }
    
    // Initialize path ID format string
    int l1 = 0;
    int l2 = 0;
    for (int i=0; i<bankNames.length; i++)
    {
      l1 = Math.max(l1,bankNames[i].length());
      l2 = Math.max(l2,memSizes[i]+1);
    }
    l2 = (int)Math.ceil(Math.log10(l2));
    this.fPatchID = String.format("%%-%ds%%0%dd",l1,l2);
  }

  // -- Getters and Setters ---------------------------------------------------

  /**
   * Gets the bank IDs. The returned array is a copy of the internal data.
   */
  public int[] getBankIDs()
  {
    return this.bankIDs.clone();
  }

  /**
   * Gets the number of patches in a bank.
   * 
   * @param m
   *          The patch bank ID, must be a valid key in {@link #bankNames}
   * @return The size of bank {@code m}
   * @throws IllegalArgumentException
   *           if {@code m} is invalid
   */
  public int getBankSize(int m)
  throws IllegalArgumentException
  {
    if (!this.patchNames.containsKey(m))
      throw SYX.IllArgExc("Invalid Argument 'm'=%d",m);
    return this.patchNames.get(m).size();
  }
  
  /**
   * Gets a patch ID (bank name followed by patch number ({@code n}+1).
   * 
   * @param m
   *          The patch bank ID, must be a valid key in {@link #bankNames}
   * @param n
   *          The zero-based patch number in bank {@code m}
   * @return The patch ID
   * @throws IllegalArgumentException
   *           if {@code m} is invalid
   * @throws IndexOutOfBoundsException
   *           if is {@code n} is out of range
   */
  public String getPatchID(int m, int n)
  throws IllegalArgumentException, IndexOutOfBoundsException
  {
    if (!this.patchNames.containsKey(m))
      throw SYX.IllArgExc("Invalid Argument 'm'=%d",m);
    return String.format(this.fPatchID,this.bankNames.get(m),n+1); 
  }

  /**
   * Gets a patch name.
   * 
   * @param m
   *          The patch bank ID, must be a valid key in {@link #bankNames}
   * @param n
   *          The zero-based patch number in bank {@code m}
   * @return The patch name
   * @throws IllegalArgumentException
   *           if {@code m} is invalid
   * @throws IndexOutOfBoundsException
   *           if is {@code n} is out of range
   */
  public String getPatchName(int m, int n)
  throws IllegalArgumentException, IndexOutOfBoundsException
  {
    if (!this.patchNames.containsKey(m))
      throw SYX.IllArgExc("Invalid Argument 'm'=%d",m);
    return this.patchNames.get(m).get(n);
  }

  /**
   * Sets a patch name.
   * 
   * @param m
   *          The patch bank ID, must be a valid key in {@link #bankNames}
   * @param n
   *          The zero-based patch number in bank {@code m}
   * @param name
   *          The patch name
   * @throws IllegalArgumentException
   *           if {@code m} is invalid
   * @throws IndexOutOfBoundsException
   *           if is {@code n} is out of range
   */
  public void setPatchName(int m, int n, String name)
  {
    if (!this.patchNames.containsKey(m))
      throw SYX.IllArgExc("Invalid Argument 'm'=%d",m);
    this.patchNames.get(m).set(n,name);
  }

  // -- Pretty-Printing -------------------------------------------------------
  
  /**
   * Pretty-prints this patch bank.
   * 
   * @param linePrefix
   *          A prefix string for printed lines (not applied to the first line)
   * @return The printed string
   */
  public String prettyPrint(String linePrefix)
  {
    String t = linePrefix!=null ? linePrefix : "";
    String s = "";

    // Get list of qualified patch names
    ArrayList<String> pn = new ArrayList<String>();
    int pnLen = 0;
    for (int m : this.bankNames.keySet())
      for (int n = 0; n<this.patchNames.get(m).size(); n++)
      {
        String patchName = this.patchNames.get(m).get(n);
        String patchID = getPatchID(m,n);
        if (patchName==null)
          patchName = "<NIL>";
        pnLen = Math.max(pnLen,patchName.length());
        String format = String.format("%%s:%%-%ds",pnLen);
        pn.add(String.format(format,patchID,patchName));
      }

    // Print in four columns    
    int C = (int)Math.pow(2,(int)((int)(Math.log(80/pnLen))/Math.log(2)));
    int L = pn.size()/C+1;
    for (int l=0; l<L; l++) // Print lines
    {
      for (int c=0; c<C; c++) // Print columns
      {
        int i = c*L+l;
        if (i<pn.size())
            s += pn.get(i)+"  ";
      }
      if (l<L-1)
        s += "\n"+t;
    }
    return s;
  }

  /**
   * Pretty-prints this patch bank
   * 
   * @return The printed string
   */
  public String prettyPrint()
  {
    return prettyPrint(null);
  }

}

// EOF