import { Body, Controller, Delete, Get, Param, Post, Put, Query } from '@nestjs/common';
import { TestService } from './test.service';

@Controller('test') 
export class TestController {
  constructor(private readonly testService: TestService) {}
    @Get('/fetch')
    async fetch(@Query('id') id : bigint){
      return await this.testService.getSalesById(id);
    }
    @Post()
    async createSales(@Body() sales: any){
      return await this.testService.createSales(sales);
    }
    @Put('/:id')
    async update(@Body() sales: any, @Param('id') id: bigint){
      return await this.testService.updateSales(sales,id);
    }
    
    @Delete('/:id')
    async delete(@Param('id') id: bigint){
      return await this.testService.deleteSales(id);
    }


}
