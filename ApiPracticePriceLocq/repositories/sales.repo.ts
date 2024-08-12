import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/sequelize';
import { Sales } from '../models/sales.model';
@Injectable()
export class SalesRepo {
  constructor(
    @InjectModel(Sales)
    private salesModel: typeof Sales
  ) {}

  async getSalesById(id: bigint) {
    return await this.salesModel.findOne({ where: { id } });
  }

  async createSales(sales: Sales){
    return await sales.save();
  }

  async updateSales(item: string, amount: number, id: bigint){
    return await this.salesModel.update({item, amount}, {where: {id}});
  }

  async deleteSales(id: bigint){
    return await this.salesModel.destroy({where: {id}});
  }

}
