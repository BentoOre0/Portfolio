import { Injectable } from '@nestjs/common';
import { Sales } from './models/sales.model';
import { SalesRepo } from './repositories/sales.repo';

@Injectable()
export class TestService {
    constructor(private readonly salesRepo: SalesRepo) {

    }
    async getSalesById(id: bigint){
        const variable = await this.salesRepo.getSalesById(id);
        // delete variable.amount;
        return variable;
    }

    async createSales(sales: any){
        const data = new Sales();
        data.amount = sales.amount;
        data.item = sales.item;
        return await this.salesRepo.createSales(data);
    }

    async updateSales(sales: any, id : bigint){
        
        const varaa = await this.salesRepo.updateSales(sales.item, sales.amount,id);
        return await this.salesRepo.getSalesById(id);

    }

    async deleteSales(id: bigint){
        await this.salesRepo.deleteSales(id);
        return "Success";
    }
}
