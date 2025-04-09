package Models;

public class FlatInfo {

	int  Type ;
	String  Typename;
    float  sellingprice;
    int Nounits  = 0;
    
    public FlatInfo(int type , String typename , float price , int units)
    {
    	this.Type = type;
    	this.Typename = typename;
    	this.sellingprice = price;
    	this.Nounits  = units;
    }
    
    //setters and getters
    
    public void settype(int type )
    {
    	this.Type = type;
    }
    
    public int getType()
    {
    	return this.Type;
    }
    
    public void setTypeName(String typeName )
    {
    	this.Typename = typeName;
    	
    }
    public String getTypeName()
    {
    	
    	return this.Typename;
    	
    }
    
    public float getSellingPrice()
    {
    	return this.sellingprice;
    }
    
    public void setSellingPrice(float price)
    {
    	this.sellingprice = price ;
    }
    public int getNoOfUnits() {
        return this.Nounits;
    }

    public void  SetNoOfUnits(int units)
    {
       this.Nounits = units;
    }
    
  
    
    public void getinfo()
    {
    	System.out.println("*******************************");
    	System.out.println("*        FLAT DETAILS        *");
    	System.out.println("*******************************");
    	System.out.println("* Type: " + this.Type + "                   *");
    	System.out.println("* Type Name: " + this.Typename + "           *");
    	System.out.println("* Number of Units: " + this.Nounits + "       *");
    	System.out.println("* Selling Price: " + this.sellingprice + "   *");
    	System.out.println("*******************************");
    }
	

}
