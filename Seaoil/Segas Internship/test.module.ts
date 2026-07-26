import { Module } from '@nestjs/common';
import { SequelizeModule } from '@nestjs/sequelize';
import { Sales } from './models/sales.model';
import { SalesRepo } from './repositories/sales.repo';
import { TestController } from './test.controller';
import { TestService } from './test.service';
@Module({
  imports: [SequelizeModule.forFeature([Sales])],
  controllers: [TestController],
  providers: [TestService, SalesRepo]
})
export class TestModule {}
